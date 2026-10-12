package dev.alexisreyna.artisanalbrew.infrastructure.service;

import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import dev.alexisreyna.artisanalbrew.application.service.SolverDenialReason;
import dev.alexisreyna.artisanalbrew.application.service.SolverIntent;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyDecision;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

/**
 * Port of {@code SolverPolicyService} ({@code Infrastructure/Services}).
 * Fail-closed: refuses unless the pair is allowed, size fits, not expired,
 * solver named, and payout covers the minimum.
 */
@Service
public class DefaultSolverPolicyService implements SolverPolicyService {
    private static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    private final CrossChainSolverOptions options;

    public DefaultSolverPolicyService(CrossChainSolverOptions options) {
        this.options = options;
    }

    @Override
    public SolverPolicyDecision evaluate(SolverIntent intent, String solverAddress, Instant now) {
        if (intent.orderId() == null || intent.orderId().isBlank()
                || intent.sourceToken() == null || intent.sourceToken().isBlank()
                || intent.destinationToken() == null || intent.destinationToken().isBlank()
                || intent.amountIn() == null || intent.amountIn().signum() <= 0) {
            return SolverPolicyDecision.deny(SolverDenialReason.INVALID_REQUEST,
                    "Intent is missing required fields or has a non-positive amountIn.");
        }
        if (!options.canPrice()) {
            return SolverPolicyDecision.deny(SolverDenialReason.NOT_CONFIGURED,
                    "Cross-chain solver is not configured.");
        }
        if (!options.isTokenPairAllowed(intent.sourceToken(), intent.destinationToken())) {
            return SolverPolicyDecision.deny(SolverDenialReason.DISALLOWED_TOKEN_PAIR,
                    "Token pair (" + intent.sourceToken() + " -> " + intent.destinationToken()
                            + ") is not on the solver's allowlist.");
        }
        BigInteger maxAmountIn = options.getMaxAmountIn().multiply(new BigDecimal("1000000000000000000"))
                .toBigInteger();
        if (intent.amountIn().compareTo(maxAmountIn) > 0) {
            return SolverPolicyDecision.deny(SolverDenialReason.AMOUNT_TOO_LARGE,
                    "amountIn " + intent.amountIn() + " exceeds the solver's configured maximum " + maxAmountIn + ".");
        }
        if (now.getEpochSecond() >= intent.deadline().longValue()) {
            return SolverPolicyDecision.deny(SolverDenialReason.EXPIRED,
                    "Intent deadline " + intent.deadline() + " has passed.");
        }
        if (!intent.allowedSolver().equalsIgnoreCase(ZERO_ADDRESS)
                && !intent.allowedSolver().equalsIgnoreCase(solverAddress)) {
            return SolverPolicyDecision.deny(SolverDenialReason.NOT_ALLOWED_SOLVER,
                    "Intent restricts filling to " + intent.allowedSolver() + ", this solver is " + solverAddress + ".");
        }
        BigInteger amountOut = intent.amountIn().multiply(BigInteger.valueOf(options.getMaxOutputBps()))
                .divide(BigInteger.valueOf(10_000));
        if (amountOut.compareTo(intent.minAmountOut()) < 0) {
            return SolverPolicyDecision.deny(SolverDenialReason.OUTPUT_BELOW_MINIMUM,
                    "At " + options.getMaxOutputBps() + " bps this solver would pay " + amountOut
                            + ", below the required minAmountOut " + intent.minAmountOut() + ".");
        }
        return SolverPolicyDecision.approve(amountOut);
    }
}

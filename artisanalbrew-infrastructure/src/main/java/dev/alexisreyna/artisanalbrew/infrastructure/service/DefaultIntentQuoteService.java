package dev.alexisreyna.artisanalbrew.infrastructure.service;

import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteRequest;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteResult;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteService;
import dev.alexisreyna.artisanalbrew.application.service.SolverIntent;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyDecision;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyService;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;

/**
 * Port of {@code IntentQuoteService} ({@code Infrastructure/Services}).
 * Delegates to the solver's own policy — never reprices.
 */
@Service
public class DefaultIntentQuoteService implements IntentQuoteService {
    private static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";
    private static final String PREVIEW_ORDER_ID_SENTINEL = "0x" + "0".repeat(63) + "1";

    private final SolverPolicyService policy;
    private final CrossChainSolverOptions options;
    private final Clock clock;

    public DefaultIntentQuoteService(SolverPolicyService policy, CrossChainSolverOptions options, Clock clock) {
        this.policy = policy;
        this.options = options;
        this.clock = clock;
    }

    @Override
    public IntentQuoteResult getQuote(IntentQuoteRequest request) {
        Instant now = clock.instant();
        SolverIntent synthetic = new SolverIntent(
                PREVIEW_ORDER_ID_SENTINEL, ZERO_ADDRESS,
                request.sourceToken(), request.amountIn(), BigInteger.ZERO,
                request.destinationToken(), ZERO_ADDRESS, BigInteger.ZERO,
                BigInteger.valueOf(now.plusSeconds(3600).getEpochSecond()), BigInteger.ZERO,
                ZERO_ADDRESS, "");
        SolverPolicyDecision decision = policy.evaluate(synthetic, ZERO_ADDRESS, now);
        return decision.approved()
                ? IntentQuoteResult.quotable(decision.amountOut(), options.getSourceChainKey(), options.getDestinationChainKey())
                : IntentQuoteResult.notQuotable(decision.reason(), decision.detail());
    }
}

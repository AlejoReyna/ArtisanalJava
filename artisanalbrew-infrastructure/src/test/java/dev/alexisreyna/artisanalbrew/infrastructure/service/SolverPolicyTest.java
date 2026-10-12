package dev.alexisreyna.artisanalbrew.infrastructure.service;

import dev.alexisreyna.artisanalbrew.application.service.SolverIntent;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyDecision;
import dev.alexisreyna.artisanalbrew.application.service.SolverPolicyService;
import dev.alexisreyna.artisanalbrew.application.service.SolverDenialReason;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteRequest;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteResult;
import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolverPolicyTest {

    private static CrossChainSolverOptions options() {
        CrossChainSolverOptions options = new CrossChainSolverOptions();
        options.setEnabled(true);
        options.setSourceChainKey("ethereum-sepolia");
        options.setDestinationChainKey("base-sepolia");
        options.setAllowedTokenPairs(List.of(
                new CrossChainSolverOptions.TokenPair("0xaaa", "0xbbb")));
        options.setMaxAmountIn(new BigDecimal("1000"));
        options.setMaxOutputBps(9900);
        return options;
    }

    private static SolverIntent intent(String orderId, String amountIn, long deadline) {
        return new SolverIntent(orderId, "0xuser", "0xaaa", new BigInteger(amountIn),
                BigInteger.valueOf(84532), "0xbbb", "0xrecv", BigInteger.ONE,
                BigInteger.valueOf(deadline),
                BigInteger.ZERO, "0x0000000000000000000000000000000000000000", "0xsubmit");
    }

    @Test
    void failClosedAndPriced() {
        var policy = new DefaultSolverPolicyService(options());
        Instant now = Instant.ofEpochSecond(1_700_000_000);
        var ok = policy.evaluate(intent("0xorder", "100", 1_800_000_000L), "0xsolver", now);
        assertThat(ok.approved()).isTrue();
        assertThat(ok.amountOut()).isEqualTo(new BigInteger("99"));

        assertThat(policy.evaluate(intent("", "100", 1_800_000_000L), "0xsolver", now).reason())
                .isEqualTo(SolverDenialReason.INVALID_REQUEST);
        assertThat(policy.evaluate(intent("0xorder", "100", 1_000_000_000L), "0xsolver", now).reason())
                .isEqualTo(SolverDenialReason.EXPIRED);

        var disabled = new DefaultSolverPolicyService(new CrossChainSolverOptions());
        assertThat(disabled.evaluate(intent("0xorder", "100", 1_800_000_000L), "0xsolver", now).reason())
                .isEqualTo(SolverDenialReason.NOT_CONFIGURED);

        var quoteService = new DefaultIntentQuoteService(policy, options(),
                Clock.fixed(now, ZoneOffset.UTC));
        IntentQuoteResult quote = quoteService.getQuote(
                new IntentQuoteRequest("0xaaa", "0xbbb", new BigInteger("100")));
        assertThat(quote.fillable()).isTrue();
        assertThat(quote.amountOut()).isEqualTo(new BigInteger("99"));
        assertThat(quote.sourceChainKey()).isEqualTo("ethereum-sepolia");
    }
}

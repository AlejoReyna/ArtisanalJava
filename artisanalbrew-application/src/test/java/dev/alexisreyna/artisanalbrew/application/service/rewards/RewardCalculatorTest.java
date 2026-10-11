package dev.alexisreyna.artisanalbrew.application.service.rewards;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RewardCalculatorTest {

    @Test
    void dailyRewardMatchesDotNetFormula() {
        // 100 * (5.2 / 365 / 100) = 0.014246575...
        BigDecimal reward = RewardCalculator.dailyRewardFromPaymentToken(
                new BigDecimal("100"), new BigDecimal("5.2"));
        assertThat(reward).isEqualByComparingTo("0.014246575342465753");
    }

    @Test
    void nonPositiveInputsYieldZero() {
        assertThat(RewardCalculator.dailyRewardFromPaymentToken(BigDecimal.ZERO, new BigDecimal("5.2")))
                .isEqualByComparingTo("0");
        assertThat(RewardCalculator.dailyRewardFromPaymentToken(new BigDecimal("10"), BigDecimal.ZERO))
                .isEqualByComparingTo("0");
        assertThat(RewardCalculator.loyaltyFromVerifiedPayment(new BigDecimal("-1")))
                .isEqualByComparingTo("0");
    }

    @Test
    void loyaltyPassesVerifiedAmountThrough() {
        assertThat(RewardCalculator.loyaltyFromVerifiedPayment(new BigDecimal("2.5")))
                .isEqualByComparingTo("2.5");
    }
}

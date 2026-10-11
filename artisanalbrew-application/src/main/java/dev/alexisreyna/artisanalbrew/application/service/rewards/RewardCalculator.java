package dev.alexisreyna.artisanalbrew.application.service.rewards;

import java.math.BigDecimal;

/** Port of {@code Rewards/RewardCalculator.cs}. */
public final class RewardCalculator {
    private RewardCalculator() {
    }

    public static BigDecimal dailyRewardFromPaymentToken(BigDecimal paymentTokenBalance, BigDecimal aprPercent) {
        if (paymentTokenBalance == null || paymentTokenBalance.signum() <= 0
                || aprPercent == null || aprPercent.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return paymentTokenBalance.multiply(aprPercent)
                .divide(new BigDecimal("36500"), 18, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Loyalty mint size derives from the verified payment, never a client amount.
     */
    public static BigDecimal loyaltyFromVerifiedPayment(BigDecimal verifiedPaymentAmount) {
        return verifiedPaymentAmount != null && verifiedPaymentAmount.signum() > 0
                ? verifiedPaymentAmount
                : BigDecimal.ZERO;
    }
}

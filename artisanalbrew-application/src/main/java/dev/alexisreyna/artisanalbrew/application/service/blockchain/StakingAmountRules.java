package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Port of {@code Blockchain/StakingAmountRules.cs}. */
public final class StakingAmountRules {
    private static final int DEFAULT_TOKEN_DECIMALS = 18;

    private StakingAmountRules() {
    }

    public static boolean isValidStakeAmount(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    public static boolean isValidUnstakeAmount(BigDecimal amount, BigDecimal stakedBalance) {
        return amount != null && amount.signum() > 0
                && stakedBalance != null && stakedBalance.signum() > 0
                && amount.compareTo(stakedBalance) <= 0;
    }

    /**
     * Scales to smallest units as a decimal string. Truncates rather than rounds:
     * the on-chain value is the truth.
     */
    public static String toRawAmount(BigDecimal value, int decimals) {
        BigDecimal scaled = value.multiply(new BigDecimal(BigInteger.TEN.pow(decimals)));
        return scaled.toBigInteger().toString();
    }

    public static String toRawAmount(BigDecimal value) {
        return toRawAmount(value, DEFAULT_TOKEN_DECIMALS);
    }
}

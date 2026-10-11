package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code SolanaVaultAccountState} in {@code Blockchain/SolanaAccountCodec.cs}. */
public record SolanaVaultAccountState(
        int cafeDecimals,
        int coffeeDecimals,
        long totalShares,
        java.math.BigInteger rewardPerShare,
        long rewardRate,
        long periodFinish,
        long lastRewardSlot) {
}

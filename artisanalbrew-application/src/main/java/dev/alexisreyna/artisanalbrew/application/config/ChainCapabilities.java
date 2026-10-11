package dev.alexisreyna.artisanalbrew.application.config;

/** Port of {@code ChainCapabilities} in {@code Configuration/ChainFamily.cs}. */
public record ChainCapabilities(
        boolean walletLogin,
        boolean liquidStaking,
        boolean legacyExit,
        boolean faucet,
        boolean marketplacePayment,
        boolean rewardMinting,
        boolean agenticCommerce,
        boolean agenticSessionPayments) {

    public ChainCapabilities() {
        this(false, false, false, false, false, false, false, false);
    }
}

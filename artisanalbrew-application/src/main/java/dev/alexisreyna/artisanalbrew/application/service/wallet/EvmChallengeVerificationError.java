package dev.alexisreyna.artisanalbrew.application.service.wallet;

/** Port of {@code WalletChallengeVerificationError} in {@code Web/Services/Wallet/WalletChallengeService.cs}. */
public enum EvmChallengeVerificationError {
    NONE,
    EXPIRED,
    MISMATCH,
    INVALID_SIGNATURE
}

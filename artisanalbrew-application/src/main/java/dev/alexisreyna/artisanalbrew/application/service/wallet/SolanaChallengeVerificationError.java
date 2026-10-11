package dev.alexisreyna.artisanalbrew.application.service.wallet;

/** Port of {@code SolanaChallengeVerificationError} in {@code Wallet/ISolanaWalletChallengeService.cs}. */
public enum SolanaChallengeVerificationError {
    NONE,
    EXPIRED,
    MISMATCH,
    INVALID_SIGNATURE
}

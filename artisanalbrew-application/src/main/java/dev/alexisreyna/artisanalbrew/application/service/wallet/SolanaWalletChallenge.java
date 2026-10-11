package dev.alexisreyna.artisanalbrew.application.service.wallet;

import java.time.OffsetDateTime;

/** Port of {@code SolanaWalletChallenge} in {@code Wallet/ISolanaWalletChallengeService.cs}. */
public record SolanaWalletChallenge(String message, String nonce, OffsetDateTime issuedAt, OffsetDateTime expiresAt) {
}

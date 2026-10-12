package dev.alexisreyna.artisanalbrew.application.service.wallet;

import java.time.OffsetDateTime;

/** Port of {@code WalletChallenge} in {@code Web/Services/Wallet/WalletChallengeService.cs}. */
public record EvmWalletChallenge(String message, String nonce, OffsetDateTime issuedAt, OffsetDateTime expiresAt) {
}

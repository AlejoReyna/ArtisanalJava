package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;
import java.time.Instant;

/** Port of {@code SolanaFaucetClaimResult} in {@code Blockchain/ISolanaFaucetService.cs}. */
public record SolanaFaucetClaimResult(
        boolean success,
        String signature,
        String explorerUrl,
        BigDecimal amount,
        String error,
        Instant nextClaimAtUtc) {
}

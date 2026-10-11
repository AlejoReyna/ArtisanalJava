package dev.alexisreyna.artisanalbrew.application.service.rewards;

import java.math.BigDecimal;

/** Port of {@code LoyaltyMintCommand} in {@code Rewards/ILoyaltyMintService.cs}. */
public record LoyaltyMintCommand(
        String walletAddress,
        BigDecimal amount,
        BigDecimal paymentAmount,
        String paymentTransactionHash,
        String allocationName) {
}

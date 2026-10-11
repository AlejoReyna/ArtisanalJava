package dev.alexisreyna.artisanalbrew.application.service.rewards;

import java.math.BigDecimal;

/** Port of {@code LoyaltyMintStatus} in {@code Rewards/ILoyaltyMintService.cs}. */
public enum LoyaltyMintStatus {
    MINTED,
    ALREADY_CLAIMED,
    PENDING_CONFIRMATIONS,
    PAYMENT_NOT_VERIFIED,
    MINTING_NOT_CONFIGURED,
    MINT_FAILED
}

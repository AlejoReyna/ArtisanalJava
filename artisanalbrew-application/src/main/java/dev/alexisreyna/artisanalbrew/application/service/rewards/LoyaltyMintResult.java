package dev.alexisreyna.artisanalbrew.application.service.rewards;

/** Port of {@code LoyaltyMintResult} in {@code Rewards/ILoyaltyMintService.cs}. */
public record LoyaltyMintResult(LoyaltyMintStatus status, String mintTransactionHash, String error) {

    public static LoyaltyMintResult minted(String mintTransactionHash) {
        return new LoyaltyMintResult(LoyaltyMintStatus.MINTED, mintTransactionHash, null);
    }

    public static final LoyaltyMintResult ALREADY_CLAIMED =
            new LoyaltyMintResult(LoyaltyMintStatus.ALREADY_CLAIMED, null, null);
    public static final LoyaltyMintResult PENDING_CONFIRMATIONS =
            new LoyaltyMintResult(LoyaltyMintStatus.PENDING_CONFIRMATIONS, null, null);
    public static final LoyaltyMintResult PAYMENT_NOT_VERIFIED =
            new LoyaltyMintResult(LoyaltyMintStatus.PAYMENT_NOT_VERIFIED, null, null);
    public static final LoyaltyMintResult MINTING_NOT_CONFIGURED =
            new LoyaltyMintResult(LoyaltyMintStatus.MINTING_NOT_CONFIGURED, null, null);

    public static LoyaltyMintResult mintFailed(String error) {
        return new LoyaltyMintResult(LoyaltyMintStatus.MINT_FAILED, null, error);
    }
}

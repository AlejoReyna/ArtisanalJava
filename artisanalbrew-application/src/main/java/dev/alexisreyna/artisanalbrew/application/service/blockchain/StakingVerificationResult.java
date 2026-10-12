package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code Blockchain/StakingVerificationResult.cs}. */
public record StakingVerificationResult(
        TransactionVerificationStatus status,
        BigDecimal amount,
        int confirmations,
        int requiredConfirmations) {

    public boolean verified() {
        return status == TransactionVerificationStatus.VERIFIED;
    }

    public static final StakingVerificationResult FAILED =
            new StakingVerificationResult(TransactionVerificationStatus.FAILED, BigDecimal.ZERO, 0, 0);

    public static StakingVerificationResult verified(BigDecimal amount) {
        return new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, amount, 0, 0);
    }

    public static StakingVerificationResult pending(int confirmations, int requiredConfirmations) {
        return new StakingVerificationResult(
                TransactionVerificationStatus.PENDING_CONFIRMATIONS, BigDecimal.ZERO, confirmations, requiredConfirmations);
    }
}

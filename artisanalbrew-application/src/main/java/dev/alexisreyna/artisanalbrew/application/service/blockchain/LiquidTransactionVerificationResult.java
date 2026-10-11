package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code LiquidTransactionVerificationResult} in {@code Blockchain/LiquidStakingModels.cs}. */
public record LiquidTransactionVerificationResult(
        boolean verified,
        TransactionVerificationStatus status,
        BigDecimal assetAmount,
        BigDecimal shareAmount,
        BigDecimal rewardAmount,
        long blockNumber,
        int operationIndex,
        String error) {

    public static LiquidTransactionVerificationResult failed(String error) {
        return new LiquidTransactionVerificationResult(false, TransactionVerificationStatus.FAILED,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, error);
    }
}

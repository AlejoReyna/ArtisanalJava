package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code ILiquidStakingGateway} in {@code Blockchain/LiquidStakingModels.cs}. */
public interface LiquidStakingGateway {
    LiquidStakingDashboard getDashboard(String chainKey, String walletIdentifier);

    LiquidTransactionVerificationResult verify(String chainKey, String walletIdentifier,
                                               String transactionId, LiquidStakingOperation operation,
                                               BigDecimal expectedAmount);
}

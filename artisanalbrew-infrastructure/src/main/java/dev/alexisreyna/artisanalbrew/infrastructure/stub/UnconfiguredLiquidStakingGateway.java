package dev.alexisreyna.artisanalbrew.infrastructure.stub;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingDashboard;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingOperation;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidTransactionVerificationResult;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Fail-closed liquid-staking gateway: unconfigured chain, failed verification. */
public class UnconfiguredLiquidStakingGateway implements LiquidStakingGateway {

    @Override
    public LiquidStakingDashboard getDashboard(String chainKey, String walletIdentifier) {
        LiquidStakingDashboard dashboard = new LiquidStakingDashboard();
        dashboard.setChainKey(chainKey);
        dashboard.setWalletIdentifier(walletIdentifier);
        dashboard.setConfigured(false);
        dashboard.setUnavailableReason("Liquid staking is not deployed on this chain yet.");
        dashboard.setReadAtUtc(OffsetDateTime.now());
        return dashboard;
    }

    @Override
    public LiquidTransactionVerificationResult verify(String chainKey, String walletIdentifier, String transactionId,
                                                      LiquidStakingOperation operation, BigDecimal expectedAmount) {
        return LiquidTransactionVerificationResult.failed("Liquid staking is not deployed on this chain yet.");
    }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerRepository;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerWriteResult;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Port of {@code LiquidStakingLedgerService} in {@code Blockchain/LiquidStakingLedgerService.cs}.
 * Multi-chain path: recording is idempotent, repeats are successes.
 */
public final class DefaultLiquidStakingLedgerService implements LiquidStakingLedgerService {
    private final LiquidStakingGateway gateway;
    private final StakingLedgerRepository ledger;

    public DefaultLiquidStakingLedgerService(LiquidStakingGateway gateway, StakingLedgerRepository ledger) {
        this.gateway = gateway;
        this.ledger = ledger;
    }

    @Override
    public RecordResult record(ChainDefinition chain, String walletIdentifier, String transactionId,
                               LiquidStakingOperation operation, BigDecimal expectedAmount) {
        LiquidTransactionVerificationResult verification =
                gateway.verify(chain.getKey(), walletIdentifier, transactionId, operation, expectedAmount);
        if (verification.status() == TransactionVerificationStatus.PENDING_CONFIRMATIONS) {
            return RecordResult.PENDING_CONFIRMATIONS;
        }
        if (!verification.verified()) {
            return RecordResult.verificationFailed(verification.error());
        }
        StakingLedgerEntry existing =
                ledger.findByOperation(chain.getKey(), transactionId, verification.operationIndex()).orElse(null);
        if (existing != null) {
            return RecordResult.alreadyRecorded(existing);
        }
        StakingLedgerEntry entry = buildEntry(chain, walletIdentifier, transactionId, operation, verification);
        StakingLedgerWriteResult write = ledger.addIfAbsent(entry);
        if (write.added()) {
            return RecordResult.recorded(entry);
        }
        return write.existing() != null
                ? RecordResult.alreadyRecorded(write.existing())
                : RecordResult.CONFLICT;
    }

    private static StakingLedgerEntry buildEntry(ChainDefinition chain, String walletIdentifier, String transactionId,
                                                 LiquidStakingOperation operation,
                                                 LiquidTransactionVerificationResult verification) {
        // Solana identifies the pool by program address; EVM by the vault contract.
        String poolIdentifier = chain.getFamily() == ChainFamily.SOLANA
                ? chain.getDeployment().getProgram()
                : chain.getDeployment().getLiquidVault();
        Instant recordedAt = Instant.now();
        return StakingLedgerEntry.create(chain.getKey(), transactionId, verification.operationIndex(), e -> {
            e.setWalletAddress(walletIdentifier);
            e.setFamily(chain.familyName());
            e.setActionType(toActionType(operation));
            // Reward funding moves no principal: headline falls back to the reward.
            e.setAmount(verification.assetAmount().signum() != 0
                    ? verification.assetAmount() : verification.rewardAmount());
            e.setAssetAmount(verification.assetAmount());
            e.setShareAmount(verification.shareAmount());
            e.setRewardAmount(verification.rewardAmount());
            e.setRawAssetAmount(StakingAmountRules.toRawAmount(
                    verification.assetAmount(), chain.getDeployment().getCafeDecimals()));
            e.setRawShareAmount(StakingAmountRules.toRawAmount(
                    verification.shareAmount(), chain.getDeployment().getStCafeDecimals()));
            e.setRawRewardAmount(StakingAmountRules.toRawAmount(
                    verification.rewardAmount(), chain.getDeployment().getCoffeeDecimals()));
            e.setChainId(chain.getEvmChainId() == null ? 0 : chain.getEvmChainId());
            e.setNetworkName(chain.getDisplayName());
            e.setPaymentTokenContract(chain.getDeployment().getCafe());
            e.setStakingPoolContract(poolIdentifier);
            e.setAssetIdentifier(chain.getDeployment().getCafe());
            e.setReceiptIdentifier(chain.getDeployment().getStCafe());
            e.setRewardIdentifier(chain.getDeployment().getCoffee());
            e.setVaultOrProgramIdentifier(poolIdentifier);
            e.setBlockOrSlot(verification.blockNumber());
            e.setVerified(true);
            e.setVerificationState("verified");
            e.setExplorerUrl(chain.getExplorerTransactionTemplate().replace("{0}", transactionId));
            e.setRecordedAtUtc(recordedAt);
            e.setOccurredAtUtc(recordedAt);
        });
    }

    private static String toActionType(LiquidStakingOperation operation) {
        return switch (operation) {
            case DEPOSIT -> "deposit";
            case REDEEM -> "redeem";
            case CLAIM -> "claim";
            default -> "reward_funding";
        };
    }
}

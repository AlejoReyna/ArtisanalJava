package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerRepository;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerWriteResult;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Port of {@code StakingLedgerService} in {@code Blockchain/StakingLedgerService.cs}.
 * Single-chain path: a repeat submission is a conflict, not a success.
 */
public final class DefaultStakingLedgerService implements StakingLedgerService {
    private static final String LEGACY_CHAIN_KEY = "ethereum-sepolia";
    private static final String LEGACY_FAMILY = "Evm";

    private final CoffeeWeb3Service web3;
    private final StakingLedgerRepository ledger;

    public DefaultStakingLedgerService(CoffeeWeb3Service web3, StakingLedgerRepository ledger) {
        this.web3 = web3;
        this.ledger = ledger;
    }

    @Override
    public RecordResult record(BlockchainNetworkOptions chain, String walletAddress, String transactionHash,
                               StakingTransactionType transactionType, BigDecimal expectedAmount) {
        // Hash-wide guard: one hash recorded once through this endpoint, on any chain.
        if (ledger.existsByTransactionHash(transactionHash)) {
            return RecordResult.ALREADY_RECORDED;
        }
        StakingVerificationResult verification =
                web3.verifyStakingTransaction(transactionHash, walletAddress, expectedAmount, transactionType);
        if (verification.status() == TransactionVerificationStatus.PENDING_CONFIRMATIONS) {
            return RecordResult.pendingConfirmations(verification);
        }
        if (!verification.verified()) {
            return RecordResult.VERIFICATION_FAILED;
        }
        StakingLedgerEntry entry = buildEntry(chain, walletAddress, transactionHash, transactionType, verification.amount());
        StakingLedgerWriteResult write = ledger.addIfAbsent(entry);
        return write.added() ? RecordResult.recorded(entry) : RecordResult.ALREADY_RECORDED;
    }

    @Override
    public List<StakingLedgerEntry> getRecentActivity(String walletAddress, int take) {
        if (walletAddress == null || walletAddress.isBlank()) {
            return List.of();
        }
        return ledger.listByWallet(walletAddress, take);
    }

    private static StakingLedgerEntry buildEntry(BlockchainNetworkOptions chain, String walletAddress,
                                                 String transactionHash, StakingTransactionType transactionType,
                                                 BigDecimal amount) {
        StakingLedgerEntry entry = StakingLedgerEntry.create(LEGACY_CHAIN_KEY, transactionHash, 0, e -> {
            e.setWalletAddress(walletAddress);
            e.setFamily(LEGACY_FAMILY);
            e.setActionType(toActionType(transactionType));
            e.setAmount(amount);
            e.setChainId(chain.getChainId());
            e.setNetworkName(chain.getNetworkName());
            e.setStakingPoolContract(chain.getStakingPoolContract());
            e.setVaultOrProgramIdentifier(chain.getStakingPoolContract());
            e.setVerified(true);
            e.setVerificationState("verified");
            e.setExplorerUrl(explorerTxUrl(chain, transactionHash));
            e.setRecordedAtUtc(Instant.now());
        });
        // Claims pay rewards; stakes move principal — different columns and contracts.
        if (transactionType == StakingTransactionType.CLAIM) {
            entry.setRewardAmount(amount);
            entry.setRawRewardAmount(StakingAmountRules.toRawAmount(amount));
            entry.setPaymentTokenContract(chain.getCoffeeCoinContract());
            entry.setRewardIdentifier(chain.getCoffeeCoinContract());
        } else {
            entry.setAssetAmount(amount);
            entry.setShareAmount(amount);
            entry.setRawAssetAmount(StakingAmountRules.toRawAmount(amount));
            entry.setRawShareAmount(StakingAmountRules.toRawAmount(amount));
            entry.setPaymentTokenContract(chain.effectivePaymentTokenContract());
            entry.setAssetIdentifier(chain.effectivePaymentTokenContract());
        }
        return entry;
    }

    private static String toActionType(StakingTransactionType transactionType) {
        return switch (transactionType) {
            case STAKE -> "stake";
            case UNSTAKE -> "unstake";
            default -> "claim";
        };
    }

    private static String explorerTxUrl(BlockchainNetworkOptions chain, String transactionHash) {
        String explorer = chain.getExplorerUrl() == null ? "" : chain.getExplorerUrl().trim();
        if (explorer.isEmpty()) {
            return "";
        }
        return explorer.replaceAll("/+$", "") + "/tx/" + transactionHash;
    }
}

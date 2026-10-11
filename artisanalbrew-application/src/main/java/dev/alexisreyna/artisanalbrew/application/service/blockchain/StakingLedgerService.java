package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

import java.math.BigDecimal;
import java.util.List;

/** Port of {@code IStakingLedgerService} + result in {@code Blockchain/IStakingLedgerService.cs}. */
public interface StakingLedgerService {

    enum RecordStatus {
        RECORDED,
        /** Repeat submission — kept as conflict (not success) on the single-chain path. */
        ALREADY_RECORDED,
        PENDING_CONFIRMATIONS,
        VERIFICATION_FAILED
    }

    record RecordResult(RecordStatus status, StakingLedgerEntry entry, StakingVerificationResult verification) {
        public static RecordResult recorded(StakingLedgerEntry entry) {
            return new RecordResult(RecordStatus.RECORDED, entry, null);
        }

        public static final RecordResult ALREADY_RECORDED =
                new RecordResult(RecordStatus.ALREADY_RECORDED, null, null);

        public static RecordResult pendingConfirmations(StakingVerificationResult verification) {
            return new RecordResult(RecordStatus.PENDING_CONFIRMATIONS, null, verification);
        }

        public static final RecordResult VERIFICATION_FAILED =
                new RecordResult(RecordStatus.VERIFICATION_FAILED, null, null);
    }

    RecordResult record(dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions chain,
                        String walletAddress, String transactionHash,
                        StakingTransactionType transactionType, BigDecimal expectedAmount);

    List<StakingLedgerEntry> getRecentActivity(String walletAddress, int take);

    default List<StakingLedgerEntry> getRecentActivity(String walletAddress) {
        return getRecentActivity(walletAddress, 8);
    }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

import java.math.BigDecimal;

/** Port of {@code ILiquidStakingLedgerService} + result in {@code Blockchain/ILiquidStakingLedgerService.cs}. */
public interface LiquidStakingLedgerService {

    /** RECORDED and ALREADY_RECORDED are both successes — recording is idempotent. */
    enum RecordStatus {
        RECORDED,
        ALREADY_RECORDED,
        PENDING_CONFIRMATIONS,
        VERIFICATION_FAILED,
        CONFLICT
    }

    record RecordResult(RecordStatus status, StakingLedgerEntry entry, String error) {
        public static RecordResult recorded(StakingLedgerEntry entry) {
            return new RecordResult(RecordStatus.RECORDED, entry, null);
        }

        public static RecordResult alreadyRecorded(StakingLedgerEntry entry) {
            return new RecordResult(RecordStatus.ALREADY_RECORDED, entry, null);
        }

        public static final RecordResult PENDING_CONFIRMATIONS =
                new RecordResult(RecordStatus.PENDING_CONFIRMATIONS, null, null);

        public static RecordResult verificationFailed(String error) {
            return new RecordResult(RecordStatus.VERIFICATION_FAILED, null, error);
        }

        public static final RecordResult CONFLICT =
                new RecordResult(RecordStatus.CONFLICT, null, null);
    }

    RecordResult record(ChainDefinition chain, String walletIdentifier, String transactionId,
                        LiquidStakingOperation operation, BigDecimal expectedAmount);
}

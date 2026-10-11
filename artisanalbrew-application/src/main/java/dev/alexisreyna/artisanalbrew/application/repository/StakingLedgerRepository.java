package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

import java.util.List;
import java.util.Optional;

/** Port of {@code Repositories/IStakingLedgerRepository.cs}. */
public interface StakingLedgerRepository {
    boolean existsByTransactionHash(String transactionHash);

    Optional<StakingLedgerEntry> findByOperation(String chainKey, String transactionHash, int operationIndex);

    /**
     * Records an entry unless the same operation is present; returns the winning
     * row so concurrent retries stay idempotent.
     */
    StakingLedgerWriteResult addIfAbsent(StakingLedgerEntry entry);

    List<StakingLedgerEntry> listByWallet(String walletAddress, int take);

    default List<StakingLedgerEntry> listByWallet(String walletAddress) {
        return listByWallet(walletAddress, 8);
    }
}

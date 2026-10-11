package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;

/**
 * Port of {@code StakingLedgerWriteResult} in {@code Repositories/StakingLedgerWriteResult.cs}.
 */
public record StakingLedgerWriteResult(boolean added, StakingLedgerEntry existing) {

    public static StakingLedgerWriteResult written() {
        return new StakingLedgerWriteResult(true, null);
    }

    public static StakingLedgerWriteResult alreadyPresent(StakingLedgerEntry existing) {
        return new StakingLedgerWriteResult(false, existing);
    }

    public static StakingLedgerWriteResult conflict() {
        return new StakingLedgerWriteResult(false, null);
    }
}

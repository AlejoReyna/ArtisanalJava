package dev.alexisreyna.artisanalbrew.domain.entities;

/**
 * Port of {@code StakingOperationIdentity} in {@code StakingLedgerEntry.cs}.
 * Durable idempotency key: (ChainKey, TransactionHash, OperationIndex).
 */
public record StakingOperationIdentity(String chainKey, String transactionHash, int operationIndex) {
    public static StakingOperationIdentity create(String chainKey, String transactionHash, int operationIndex) {
        if (chainKey == null || chainKey.isBlank() || transactionHash == null || transactionHash.isBlank() || operationIndex < 0) {
            throw new IllegalStateException("A staking ledger operation requires a chain, transaction hash, and non-negative operation index.");
        }
        return new StakingOperationIdentity(chainKey, transactionHash, operationIndex);
    }
}

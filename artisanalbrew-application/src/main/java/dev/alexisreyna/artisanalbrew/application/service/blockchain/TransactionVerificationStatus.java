package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code Blockchain/TransactionVerificationStatus.cs}. */
public enum TransactionVerificationStatus {
    VERIFIED,
    PENDING_CONFIRMATIONS,
    FAILED
}

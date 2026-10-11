package dev.alexisreyna.artisanalbrew.application.service;

import java.util.Optional;

/**
 * Port of {@code IEntryPointConfirmationReader} in {@code Services/IEntryPointConfirmationReader.cs}.
 * Bundler-independent truth: the EntryPoint's own {@code UserOperationEvent}, never a bundler flag.
 */
public interface EntryPointConfirmationReader {
    Optional<EntryPointConfirmation> findConfirmation(String chainKey, String sender, String userOpHash, String transactionHashHint);

    default Optional<EntryPointConfirmation> findConfirmation(String chainKey, String sender, String userOpHash) {
        return findConfirmation(chainKey, sender, userOpHash, null);
    }
}

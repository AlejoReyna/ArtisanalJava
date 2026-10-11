package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code EntryPointConfirmation} in {@code Services/IEntryPointConfirmationReader.cs}. */
public record EntryPointConfirmation(String transactionHash, boolean success) {
}

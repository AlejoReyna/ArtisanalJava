package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code WalletAccountResult} in {@code Services/IIdentityAccountService.cs}. */
public record WalletAccountResult(
        boolean succeeded,
        IdentityAccount account,
        boolean walletAlreadyLinkedToAnotherAccount,
        String error) {
}

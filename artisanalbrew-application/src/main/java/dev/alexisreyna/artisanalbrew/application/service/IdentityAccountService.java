package dev.alexisreyna.artisanalbrew.application.service;

import java.util.Optional;

/** Port of {@code IIdentityAccountService} in {@code Services/IIdentityAccountService.cs}. */
public interface IdentityAccountService {
    Optional<IdentityAccount> findById(String accountId);

    WalletAccountResult findOrCreateAndLinkWallet(String walletAddress, Integer walletChainId,
                                                  String walletProvider, String walletFamily);

    IdentityOperationResult delete(String accountId);

    boolean passwordSignIn(String email, String password, boolean persistent);

    void signIn(String accountId, boolean persistent);

    void signOut();
}

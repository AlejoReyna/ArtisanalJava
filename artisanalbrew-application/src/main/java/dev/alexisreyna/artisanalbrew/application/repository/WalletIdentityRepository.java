package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.WalletIdentity;

import java.util.Optional;

/** Port of {@code Repositories/IWalletIdentityRepository.cs}. */
public interface WalletIdentityRepository {
    Optional<WalletIdentity> find(String family, String normalizedAddress);

    /**
     * Creates or refreshes the address→user mapping.
     *
     * @return false when the address is already verified for a <b>different</b> user.
     */
    boolean upsert(String userId, String family, String normalizedAddress, String displayAddress, String walletProvider);
}

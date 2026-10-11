package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.SolanaFaucetClaim;

import java.util.Optional;

/** Port of {@code Repositories/ISolanaFaucetClaimRepository.cs}. */
public interface SolanaFaucetClaimRepository {
    Optional<SolanaFaucetClaim> findLatest(String chainKey, String walletAddress);

    void add(SolanaFaucetClaim claim);
}

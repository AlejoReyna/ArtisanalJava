package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.SolanaFaucetClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SolanaFaucetClaimJpaRepository extends JpaRepository<SolanaFaucetClaimEntity, UUID> {
    Optional<SolanaFaucetClaimEntity> findFirstByChainKeyAndWalletAddressOrderByClaimedAtUtcDesc(
            String chainKey, String walletAddress);
}

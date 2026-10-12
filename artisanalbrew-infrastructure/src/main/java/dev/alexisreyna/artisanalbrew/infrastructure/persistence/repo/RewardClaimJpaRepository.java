package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.RewardClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardClaimJpaRepository extends JpaRepository<RewardClaimEntity, UUID> {
    Optional<RewardClaimEntity> findFirstByWalletAddressAndClaimTypeOrderByClaimedAtUtcDesc(String walletAddress, String claimType);

    boolean existsByPaymentTransactionHash(String paymentTransactionHash);

    List<RewardClaimEntity> findByWalletAddressOrderByClaimedAtUtcDesc(String walletAddress);
}

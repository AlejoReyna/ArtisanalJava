package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletStatusEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletStatusEventJpaRepository extends JpaRepository<WalletStatusEventEntity, UUID> {
    Optional<WalletStatusEventEntity> findFirstByWalletAddressOrderByCreatedAtDesc(String walletAddress);

    List<WalletStatusEventEntity> findTop25ByOrderByCreatedAtDesc();
}

package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletIdentityJpaRepository extends JpaRepository<WalletIdentityEntity, UUID> {
    Optional<WalletIdentityEntity> findByFamilyAndNormalizedAddress(String family, String normalizedAddress);
}

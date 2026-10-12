package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationUserJpaRepository extends JpaRepository<ApplicationUserEntity, String> {
    Optional<ApplicationUserEntity> findByUserProfileId(UUID userProfileId);

    List<ApplicationUserEntity> findAllByUserProfileId(UUID userProfileId);

    Optional<ApplicationUserEntity> findByNormalizedEmail(String normalizedEmail);

    Optional<ApplicationUserEntity> findByNormalizedUserName(String normalizedUserName);

    Optional<ApplicationUserEntity> findFirstByWalletAddressIgnoreCase(String walletAddress);

    Optional<ApplicationUserEntity> findByWalletAddress(String walletAddress);
}

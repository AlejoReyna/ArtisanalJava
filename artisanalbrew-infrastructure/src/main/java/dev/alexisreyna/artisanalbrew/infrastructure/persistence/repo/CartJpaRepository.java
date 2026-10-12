package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {
    Optional<CartEntity> findByUserProfileId(UUID userProfileId);
}

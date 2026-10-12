package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {
    Optional<CouponEntity> findByNormalizedCode(String normalizedCode);
}

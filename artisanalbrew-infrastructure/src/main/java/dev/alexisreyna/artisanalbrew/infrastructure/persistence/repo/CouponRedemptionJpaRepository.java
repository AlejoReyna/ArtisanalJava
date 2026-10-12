package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CouponRedemptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CouponRedemptionJpaRepository extends JpaRepository<CouponRedemptionEntity, UUID> {
    boolean existsByCouponIdAndUserProfileId(UUID couponId, UUID userProfileId);

    List<CouponRedemptionEntity> findByUserProfileId(UUID userProfileId);

    void deleteByOrderId(UUID orderId);
}

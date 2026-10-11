package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port of {@code Repositories/ICouponRepository.cs}. */
public interface CouponRepository {
    List<Coupon> getCoupons();

    Optional<Coupon> getById(UUID id);

    Optional<Coupon> getByNormalizedCode(String normalizedCode);

    boolean hasUserRedeemed(UUID couponId, UUID userProfileId);

    void add(Coupon coupon);

    void update(Coupon coupon);

    void delete(Coupon coupon);
}

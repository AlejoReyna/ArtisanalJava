package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CouponEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponRedemptionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA adapter for {@link CouponRepository}. */
@Repository
@Transactional
public class JpaCouponRepositoryAdapter implements CouponRepository {
    private final CouponJpaRepository coupons;
    private final CouponRedemptionJpaRepository redemptions;

    public JpaCouponRepositoryAdapter(CouponJpaRepository coupons, CouponRedemptionJpaRepository redemptions) {
        this.coupons = coupons;
        this.redemptions = redemptions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getCoupons() {
        return coupons.findAll().stream().map(JpaCouponRepositoryAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Coupon> getById(UUID id) {
        return coupons.findById(id).map(JpaCouponRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Coupon> getByNormalizedCode(String normalizedCode) {
        return coupons.findByNormalizedCode(normalizedCode).map(JpaCouponRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserRedeemed(UUID couponId, UUID userProfileId) {
        return redemptions.existsByCouponIdAndUserProfileId(couponId, userProfileId);
    }

    @Override
    public void add(Coupon coupon) {
        coupons.save(toEntity(coupon));
    }

    @Override
    public void update(Coupon coupon) {
        coupons.save(toEntity(coupon));
    }

    @Override
    public void delete(Coupon coupon) {
        coupons.deleteById(coupon.getId());
    }

    static CouponEntity toEntity(Coupon coupon) {
        CouponEntity e = new CouponEntity();
        e.id = coupon.getId();
        e.code = coupon.getCode();
        e.normalizedCode = coupon.getNormalizedCode();
        e.discountPercent = coupon.getDiscountPercent();
        e.minimumOrderTotal = coupon.getMinimumOrderTotal();
        e.active = coupon.isActive();
        e.createdAt = coupon.getCreatedAt();
        e.updatedAt = coupon.getUpdatedAt();
        return e;
    }

    static Coupon toDomain(CouponEntity e) {
        return Coupon.rehydrate(e.id, e.code, e.normalizedCode, e.discountPercent,
                e.minimumOrderTotal, e.active, e.createdAt, e.updatedAt);
    }
}

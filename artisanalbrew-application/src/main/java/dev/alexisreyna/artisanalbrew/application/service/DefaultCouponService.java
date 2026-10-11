package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CouponDto;
import dev.alexisreyna.artisanalbrew.application.dto.CouponQuoteDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.application.validation.CouponValidators;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port of {@code CouponService} in {@code Services/CouponService.cs}.
 */
public final class DefaultCouponService implements CouponService {
    private final CouponRepository coupons;
    private final OrderPricingService pricing;

    public DefaultCouponService(CouponRepository coupons, OrderPricingService pricing) {
        this.coupons = coupons;
        this.pricing = pricing;
    }

    @Override
    public List<CouponDto> getCoupons() {
        return coupons.getCoupons().stream().map(DefaultCouponService::map).toList();
    }

    @Override
    public CouponDto create(CreateCouponRequest request) {
        CouponValidators.validateCreate(request);
        String normalized = Coupon.normalizeCode(request.code());
        if (coupons.getByNormalizedCode(normalized).isPresent()) {
            throw new IllegalStateException("Coupon '" + request.code().trim() + "' already exists.");
        }
        Coupon coupon = Coupon.create(request.code(), request.discountPercent(), request.minimumOrderTotal());
        coupons.add(coupon);
        return map(coupon);
    }

    @Override
    public Optional<CouponDto> update(UUID id, UpdateCouponRequest request) {
        CouponValidators.validateUpdate(request);
        Optional<Coupon> existing = coupons.getById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        String normalized = Coupon.normalizeCode(request.code());
        Optional<Coupon> clash = coupons.getByNormalizedCode(normalized);
        if (clash.isPresent() && !clash.get().getId().equals(id)) {
            throw new IllegalStateException("Coupon '" + request.code().trim() + "' already exists.");
        }
        Coupon coupon = existing.get();
        coupon.update(request.code(), request.discountPercent(), request.minimumOrderTotal(),
                request.isActive(), Instant.now());
        coupons.update(coupon);
        return Optional.of(map(coupon));
    }

    @Override
    public boolean delete(UUID id) {
        Optional<Coupon> existing = coupons.getById(id);
        if (existing.isEmpty()) {
            return false;
        }
        coupons.delete(existing.get());
        return true;
    }

    @Override
    public CouponQuoteDto quote(UUID userProfileId, List<CartItemDto> items, String code) {
        Coupon coupon = resolveRedeemableCoupon(userProfileId, items, code);
        return new CouponQuoteDto(map(coupon), pricing.calculate(items, coupon));
    }

    Coupon resolveRedeemableCoupon(UUID userProfileId, List<CartItemDto> items, String code) {
        if (userProfileId == null) {
            throw new IllegalStateException("Sign in before applying a coupon.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Add items before applying a coupon.");
        }
        Optional<Coupon> found = coupons.getByNormalizedCode(Coupon.normalizeCode(code));
        if (found.isEmpty() || !found.get().isActive()) {
            throw new IllegalStateException("Coupon code is invalid or inactive.");
        }
        Coupon coupon = found.get();
        var quote = pricing.calculate(items);
        if (!coupon.canBeRedeemedFor(quote.totalBeforeDiscount())) {
            throw new IllegalStateException(
                    "Coupon requires a minimum order total of " + coupon.getMinimumOrderTotal() + ".");
        }
        if (coupons.hasUserRedeemed(coupon.getId(), userProfileId)) {
            throw new IllegalStateException("You have already redeemed this coupon.");
        }
        return coupon;
    }

    static CouponDto map(Coupon coupon) {
        return new CouponDto(
                coupon.getId(), coupon.getCode(), coupon.getDiscountPercent(),
                coupon.getMinimumOrderTotal(), coupon.isActive(),
                coupon.getCreatedAt(), coupon.getUpdatedAt());
    }
}

package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CouponDto;
import dev.alexisreyna.artisanalbrew.application.dto.CouponQuoteDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.dto.OrderPricingDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateCouponRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port of {@code Services/ICouponService.cs}. */
public interface CouponService {
    List<CouponDto> getCoupons();

    CouponDto create(CreateCouponRequest request);

    Optional<CouponDto> update(UUID id, UpdateCouponRequest request);

    boolean delete(UUID id);

    CouponQuoteDto quote(UUID userProfileId, List<CartItemDto> items, String code);
}

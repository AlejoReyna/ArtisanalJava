package dev.alexisreyna.artisanalbrew.application.dto;

/** Port of {@code DTOs/CouponQuoteDto.cs}. */
public record CouponQuoteDto(CouponDto coupon, OrderPricingDto pricing) {
}

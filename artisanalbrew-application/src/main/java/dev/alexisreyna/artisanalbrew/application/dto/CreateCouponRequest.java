package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;

/** Port of {@code DTOs/CreateCouponRequest.cs}. */
public record CreateCouponRequest(String code, BigDecimal discountPercent, BigDecimal minimumOrderTotal) {
}

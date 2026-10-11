package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;

/** Port of {@code DTOs/UpdateCouponRequest.cs}. */
public record UpdateCouponRequest(
        String code,
        BigDecimal discountPercent,
        BigDecimal minimumOrderTotal,
        boolean isActive) {
}

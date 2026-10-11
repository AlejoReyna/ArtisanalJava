package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code DTOs/CouponDto.cs}. */
public record CouponDto(
        UUID id,
        String code,
        BigDecimal discountPercent,
        BigDecimal minimumOrderTotal,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt) {
}

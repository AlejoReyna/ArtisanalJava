package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;

/** Port of {@code DTOs/OrderPricingDto.cs}. */
public record OrderPricingDto(
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal tax,
        BigDecimal totalBeforeDiscount,
        String couponCode,
        BigDecimal couponDiscountPercent,
        BigDecimal discountAmount,
        BigDecimal total) {
}

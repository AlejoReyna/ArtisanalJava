package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Port of {@code ThisCafeteria.Domain/Entities/OrderPricing.cs}.
 *
 * <p>C# {@code decimal.Round(x, 2, AwayFromZero)} == Java {@link RoundingMode#HALF_UP}
 * for all non-negative money values handled here.</p>
 */
public record OrderPricing(
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal tax,
        BigDecimal totalBeforeDiscount,
        BigDecimal discountAmount,
        BigDecimal total) {

    public static OrderPricing calculate(List<OrderItem> items, BigDecimal shippingAmount, BigDecimal taxRate, BigDecimal discountPercent) {
        Objects.requireNonNull(items, "items");
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0 || item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
                throw new IllegalStateException("Order items must have a positive quantity and non-negative unit price.");
            }
        }
        if (shippingAmount == null || shippingAmount.signum() < 0
                || taxRate == null || taxRate.signum() < 0
                || (discountPercent != null
                    && (discountPercent.signum() < 0 || discountPercent.compareTo(new BigDecimal("100")) > 0))) {
            throw new IllegalStateException("Order pricing inputs are invalid.");
        }
        BigDecimal subtotal = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal beforeDiscount = subtotal.add(shippingAmount).add(tax);
        BigDecimal pct = discountPercent == null ? BigDecimal.ZERO : discountPercent;
        BigDecimal discount = beforeDiscount.multiply(pct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = beforeDiscount.subtract(discount).max(BigDecimal.ZERO);
        return new OrderPricing(subtotal, shippingAmount, tax, beforeDiscount, discount, total);
    }
}

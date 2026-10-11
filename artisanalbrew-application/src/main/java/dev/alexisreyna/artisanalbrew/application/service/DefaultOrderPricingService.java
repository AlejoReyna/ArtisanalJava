package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.OrderPricingDto;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderItem;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderPricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Port of {@code OrderPricingService} in {@code Services/OrderPricingService.cs}.
 * Framework-free: constructed directly, no DI annotations (wiring is Phase 5).
 */
public final class DefaultOrderPricingService implements OrderPricingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.16");
    private static final BigDecimal SHIPPING_AMOUNT = new BigDecimal("4.00");
    private static final BigDecimal DEMO_ETH_USD_RATE = new BigDecimal("3750.00");

    @Override
    public BigDecimal shippingUsd() {
        return SHIPPING_AMOUNT;
    }

    @Override
    public BigDecimal ethUsdRate() {
        return DEMO_ETH_USD_RATE;
    }

    @Override
    public OrderPricingDto calculate(List<CartItemDto> items, Coupon coupon) {
        List<OrderItem> orderItems = items.stream().map(dto -> {
            OrderItem item = new OrderItem();
            item.setQuantity(dto.quantity());
            item.setUnitPrice(dto.unitPrice());
            return item;
        }).toList();
        BigDecimal discount = coupon == null ? null : coupon.getDiscountPercent();
        OrderPricing pricing = OrderPricing.calculate(orderItems, SHIPPING_AMOUNT, TAX_RATE, discount);
        return new OrderPricingDto(
                pricing.subtotal(), pricing.shipping(), pricing.tax(), pricing.totalBeforeDiscount(),
                coupon == null ? null : coupon.getCode(),
                coupon == null ? null : coupon.getDiscountPercent(),
                pricing.discountAmount(), pricing.total());
    }

    @Override
    public BigDecimal toNativePaymentAmount(BigDecimal usdTotal, String nativeCurrencySymbol) {
        if (usdTotal == null || usdTotal.signum() <= 0) {
            throw new IllegalStateException("A payable order total is required.");
        }
        if (!"ETH".equals(nativeCurrencySymbol)) {
            throw new IllegalStateException(
                    "Checkout pricing is not yet available in the selected network's native currency.");
        }
        return usdTotal.divide(DEMO_ETH_USD_RATE, 18, RoundingMode.DOWN);
    }
}

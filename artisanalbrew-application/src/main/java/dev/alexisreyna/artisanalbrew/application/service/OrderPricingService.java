package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.OrderPricingDto;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;

import java.math.BigDecimal;
import java.util.List;

/** Port of {@code Services/IOrderPricingService.cs}. */
public interface OrderPricingService {
    BigDecimal shippingUsd();

    /** Fixed demo ETH/USD rate — not an oracle. */
    BigDecimal ethUsdRate();

    OrderPricingDto calculate(List<CartItemDto> items, Coupon coupon);

    default OrderPricingDto calculate(List<CartItemDto> items) {
        return calculate(items, null);
    }

    BigDecimal toNativePaymentAmount(BigDecimal usdTotal, String nativeCurrencySymbol);
}

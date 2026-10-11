package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultOrderPricingServiceTest {

    private final OrderPricingService pricing = new DefaultOrderPricingService();

    @Test
    void calculateAddsShippingTaxAndCoupon() {
        var items = List.of(new CartItemDto(UUID.randomUUID(), "Espresso", 2, new BigDecimal("10.00")));
        var dto = pricing.calculate(items, dev.alexisreyna.artisanalbrew.domain.entities.Coupon.create(
                "SAVE", new BigDecimal("10"), BigDecimal.ZERO));
        assertThat(dto.subtotal()).isEqualByComparingTo("20.00");
        assertThat(dto.tax()).isEqualByComparingTo("3.20");
        assertThat(dto.totalBeforeDiscount()).isEqualByComparingTo("27.20");
        assertThat(dto.discountAmount()).isEqualByComparingTo("2.72");
        assertThat(dto.total()).isEqualByComparingTo("24.48");
        assertThat(dto.couponCode()).isEqualTo("SAVE");
    }

    @Test
    void nativePaymentIsEthOnlyAndTruncates() {
        assertThat(pricing.toNativePaymentAmount(new BigDecimal("3750.00"), "ETH"))
                .isEqualByComparingTo("1.000000000000000000");
        assertThatThrownBy(() -> pricing.toNativePaymentAmount(new BigDecimal("10"), "tBNB"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> pricing.toNativePaymentAmount(BigDecimal.ZERO, "ETH"))
                .isInstanceOf(IllegalStateException.class);
    }
}

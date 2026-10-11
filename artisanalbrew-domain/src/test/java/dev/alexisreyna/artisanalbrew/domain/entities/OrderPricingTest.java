package dev.alexisreyna.artisanalbrew.domain.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPricingTest {

    private static OrderItem item(String price, int qty) {
        OrderItem i = new OrderItem();
        i.setUnitPrice(new BigDecimal(price));
        i.setQuantity(qty);
        return i;
    }

    @Test
    void calculatesSubtotalTaxDiscountAndTotal() {
        OrderPricing pricing = OrderPricing.calculate(
                List.of(item("10.00", 2), item("5.00", 1)),
                new BigDecimal("4.00"), new BigDecimal("0.16"), new BigDecimal("10"));

        assertThat(pricing.subtotal()).isEqualByComparingTo("25.00");
        assertThat(pricing.tax()).isEqualByComparingTo("4.00"); // 25 * 0.16
        assertThat(pricing.totalBeforeDiscount()).isEqualByComparingTo("33.00");
        assertThat(pricing.discountAmount()).isEqualByComparingTo("3.30");
        assertThat(pricing.total()).isEqualByComparingTo("29.70");
    }

    @Test
    void roundsHalfUpLikeDotNetAwayFromZero() {
        // 0.125 -> 0.13 under both AwayFromZero and HALF_UP (positive values).
        OrderItem i = item("0.125", 1);
        OrderPricing pricing = OrderPricing.calculate(List.of(i), BigDecimal.ZERO, BigDecimal.ZERO, null);
        assertThat(pricing.subtotal()).isEqualByComparingTo("0.125");
        assertThat(pricing.total()).isEqualByComparingTo("0.125");
    }

    @Test
    void rejectsNonPositiveQuantityAndNegativePrice() {
        assertThatThrownBy(() -> OrderPricing.calculate(
                List.of(item("1.00", 0)), BigDecimal.ZERO, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OrderPricing.calculate(
                List.of(item("-1.00", 1)), BigDecimal.ZERO, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDiscountOutside0To100() {
        assertThatThrownBy(() -> OrderPricing.calculate(
                List.of(item("1.00", 1)), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("101")))
                .isInstanceOf(IllegalStateException.class);
    }
}

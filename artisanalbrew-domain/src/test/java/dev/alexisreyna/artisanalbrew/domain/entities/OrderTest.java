package dev.alexisreyna.artisanalbrew.domain.entities;

import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private OrderPricing pricing() {
        OrderItem i = new OrderItem();
        i.setUnitPrice(new BigDecimal("10.00"));
        i.setQuantity(1);
        return OrderPricing.calculate(List.of(i), new BigDecimal("4.00"), new BigDecimal("0.16"), null);
    }

    @Test
    void placeStartsInProcessingAndCopiesPricingAndCoupon() {
        Coupon coupon = Coupon.create("WELCOME", new BigDecimal("10"), BigDecimal.ZERO);
        Order order = Order.place(UUID.randomUUID(), "ORD-1", "0xabc", List.of(), pricing(), coupon);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(order.getCouponCode()).isEqualTo("WELCOME");
        assertThat(order.getCouponDiscountPercent()).isEqualByComparingTo("10");
    }

    @Test
    void placeRequiresIdentity() {
        assertThatThrownBy(() -> Order.place(UUID.randomUUID(), " ", "0xabc", List.of(), pricing(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transitionFollowsStateMachine() {
        Order order = Order.place(UUID.randomUUID(), "ORD-1", "0xabc", List.of(), pricing(), null);
        order.transitionTo(OrderStatus.COMPLETED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.PROCESSING))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition order");
    }
}

package dev.alexisreyna.artisanalbrew.domain.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    void normalizesCodeToUpperInvariant() {
        Coupon coupon = Coupon.create("  welcome10 ", new BigDecimal("15"), new BigDecimal("20.00"));
        assertThat(coupon.getCode()).isEqualTo("welcome10");
        assertThat(coupon.getNormalizedCode()).isEqualTo("WELCOME10");
        assertThat(Coupon.normalizeCode("welcome10")).isEqualTo("WELCOME10");
    }

    @Test
    void rejectsInvalidTerms() {
        assertThatThrownBy(() -> Coupon.create("X", new BigDecimal("0"), BigDecimal.ZERO))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Coupon.create("X", new BigDecimal("101"), BigDecimal.ZERO))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redeemabilityRespectsActiveAndMinimum() {
        Coupon coupon = Coupon.create("SAVE", new BigDecimal("10"), new BigDecimal("50"));
        assertThat(coupon.canBeRedeemedFor(new BigDecimal("49.99"))).isFalse();
        assertThat(coupon.canBeRedeemedFor(new BigDecimal("50.00"))).isTrue();
        coupon.deactivate();
        assertThat(coupon.isActive()).isFalse();
        assertThat(coupon.canBeRedeemedFor(new BigDecimal("1000"))).isFalse();
    }
}

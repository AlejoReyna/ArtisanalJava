package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.CreateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorsTest {

    @Test
    void productRules() {
        var ok = new CreateProductRequest("Espresso", "Strong coffee", new BigDecimal("12.50"), 10,
                null, ProductCategory.BEANS, "Colombia", null, null);
        assertThatCode(() -> ProductValidators.validateCreate(ok)).doesNotThrowAnyException();
        var bad = new CreateProductRequest("", "x", new BigDecimal("-1"), -1, null, null, null, null, null);
        assertThatThrownBy(() -> ProductValidators.validateCreate(bad)).isInstanceOf(ValidationException.class);
    }

    @Test
    void couponCodeCharset() {
        assertThatCode(() -> CouponValidators.validateCreate(
                new CreateCouponRequest("SAVE-10_x", new BigDecimal("10"), BigDecimal.ZERO)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> CouponValidators.validateCreate(
                new CreateCouponRequest("SAVE 10!", new BigDecimal("10"), BigDecimal.ZERO)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void orderItemRules() {
        var item = new CartItemDto(UUID.randomUUID(), "Espresso", 1, new BigDecimal("12.50"));
        var ok = new CreateOrderRequest(UUID.randomUUID(), List.of(item), "0x" + "ab".repeat(20),
                "0x" + "ab".repeat(32), 11155111, "Ethereum Sepolia", new BigDecimal("0.01"),
                "https://sepolia.etherscan.io/tx/0x", Instant.now());
        assertThatCode(() -> OrderValidators.validateCreate(ok)).doesNotThrowAnyException();
        var bad = new CreateOrderRequest(UUID.randomUUID(), List.of(), "nope", "nope", 0, "", BigDecimal.ZERO, "", null);
        assertThatThrownBy(() -> OrderValidators.validateCreate(bad)).isInstanceOf(ValidationException.class);
    }

    @Test
    void displayNameTrimsAndRejectsHtml() {
        assertThatThrownBy(() -> ProfileValidators.validateUpdateDisplayName(
                new UpdateUserProfileRequest(" padded "))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ProfileValidators.validateUpdateDisplayName(
                new UpdateUserProfileRequest("<b>x</b>"))).isInstanceOf(ValidationException.class);
        assertThatCode(() -> ProfileValidators.validateUpdateDisplayName(
                new UpdateUserProfileRequest("Alex"))).doesNotThrowAnyException();
    }

    @Test
    void avatarRejectsUnknownItems() {
        var ok = new UpdateAvatarRequest("roast", "cream", "none", "dot", "none", "none");
        assertThatCode(() -> AvatarValidators.validateUpdate(ok)).doesNotThrowAnyException();
        var bad = new UpdateAvatarRequest("roast", "wings", "none", "dot", "none", "none");
        assertThatThrownBy(() -> AvatarValidators.validateUpdate(bad)).isInstanceOf(ValidationException.class);
    }
}

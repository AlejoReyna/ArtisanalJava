package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.CreateCouponRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateCouponRequest;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Port of {@code CreateCouponRequestValidator} + {@code UpdateCouponRequestValidator}. */
public final class CouponValidators {
    private static final Pattern CODE = Pattern.compile("^[A-Za-z0-9_-]+$");

    private CouponValidators() {
    }

    public static void validateCreate(CreateCouponRequest request) {
        require(request != null, "Request is required.");
        validateShared(request.code(), request.discountPercent(), request.minimumOrderTotal());
    }

    public static void validateUpdate(UpdateCouponRequest request) {
        require(request != null, "Request is required.");
        validateShared(request.code(), request.discountPercent(), request.minimumOrderTotal());
    }

    private static void validateShared(String code, BigDecimal discountPercent, BigDecimal minimumOrderTotal) {
        require(code != null && !code.isEmpty() && code.length() <= 64, "Code must be 1-64 characters.");
        require(CODE.matcher(code).matches(), "Coupon code can only contain letters, numbers, underscores, and hyphens.");
        require(discountPercent != null && discountPercent.signum() > 0
                && discountPercent.compareTo(new BigDecimal("100")) <= 0,
                "DiscountPercent must be > 0 and <= 100.");
        require(minimumOrderTotal != null && minimumOrderTotal.signum() >= 0,
                "MinimumOrderTotal must be >= 0.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}

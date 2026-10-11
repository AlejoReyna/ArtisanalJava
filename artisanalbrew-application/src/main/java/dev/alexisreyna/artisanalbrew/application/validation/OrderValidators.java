package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;

import java.util.regex.Pattern;

/** Port of {@code CreateOrderRequestValidator}. */
public final class OrderValidators {
    private static final Pattern CODE = Pattern.compile("^[A-Za-z0-9_-]+$");

    private OrderValidators() {
    }

    public static void validateCreate(CreateOrderRequest request) {
        require(request != null, "Request is required.");
        require(request.userProfileId() != null, "UserProfileId is required.");
        require(request.items() != null && !request.items().isEmpty(), "Items must not be empty.");
        require(request.walletAddress() != null && request.walletAddress().length() == 42
                && request.walletAddress().regionMatches(true, 0, "0x", 0, 2), "WalletAddress must be a 42-char 0x address.");
        require(request.paymentTransactionHash() != null && request.paymentTransactionHash().length() == 66
                && request.paymentTransactionHash().regionMatches(true, 0, "0x", 0, 2),
                "PaymentTransactionHash must be a 66-char 0x hash.");
        require(request.paymentChainId() > 0, "PaymentChainId must be > 0.");
        require(request.paymentNetworkName() != null && !request.paymentNetworkName().isEmpty()
                && request.paymentNetworkName().length() <= 80, "PaymentNetworkName must be 1-80 characters.");
        require(request.paymentEthAmount() != null && request.paymentEthAmount().signum() > 0,
                "PaymentEthAmount must be > 0.");
        require(request.paymentExplorerUrl() != null && !request.paymentExplorerUrl().isEmpty()
                && request.paymentExplorerUrl().length() <= 2_048, "PaymentExplorerUrl must be 1-2048 characters.");
        require(request.paidAtUtc() != null, "PaidAtUtc is required.");
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            require(request.couponCode().length() <= 64, "CouponCode must be <= 64 characters.");
            require(CODE.matcher(request.couponCode()).matches(),
                    "Coupon code can only contain letters, numbers, underscores, and hyphens.");
        }
        for (CartItemDto item : request.items()) {
            require(item.productId() != null, "Item ProductId is required.");
            require(item.productName() != null && !item.productName().isEmpty() && item.productName().length() <= 160,
                    "Item ProductName must be 1-160 characters.");
            require(item.quantity() > 0, "Item Quantity must be > 0.");
            require(item.unitPrice() != null && item.unitPrice().signum() > 0, "Item UnitPrice must be > 0.");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}

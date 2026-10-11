package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Port of {@code DTOs/CreateOrderRequest.cs}. */
public record CreateOrderRequest(
        UUID userProfileId,
        List<CartItemDto> items,
        String walletAddress,
        String paymentTransactionHash,
        int paymentChainId,
        String paymentNetworkName,
        BigDecimal paymentEthAmount,
        String paymentExplorerUrl,
        Instant paidAtUtc,
        String couponCode) {

    public CreateOrderRequest(UUID userProfileId, List<CartItemDto> items, String walletAddress,
                              String paymentTransactionHash, int paymentChainId, String paymentNetworkName,
                              BigDecimal paymentEthAmount, String paymentExplorerUrl, Instant paidAtUtc) {
        this(userProfileId, items, walletAddress, paymentTransactionHash, paymentChainId,
                paymentNetworkName, paymentEthAmount, paymentExplorerUrl, paidAtUtc, null);
    }
}

package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code DTOs/CommerceTransactionDto.cs}. */
public record CommerceTransactionDto(
        UUID orderId,
        String orderNumber,
        OrderStatus status,
        BigDecimal total,
        String walletAddress,
        String paymentTransactionHash,
        Integer paymentChainId,
        String paymentNetworkName,
        BigDecimal paymentEthAmount,
        String paymentExplorerUrl,
        Instant paidAtUtc,
        Instant createdAt,
        int itemCount,
        String productSummary) {
}

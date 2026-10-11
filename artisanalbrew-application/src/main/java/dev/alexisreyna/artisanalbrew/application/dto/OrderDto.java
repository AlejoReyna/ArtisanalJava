package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Port of {@code DTOs/OrderDto.cs}. */
public record OrderDto(
        UUID id,
        String orderNumber,
        UUID userProfileId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal tax,
        String couponCode,
        BigDecimal couponDiscountPercent,
        BigDecimal discountAmount,
        BigDecimal total,
        String walletAddress,
        String paymentTransactionHash,
        Integer paymentChainId,
        String paymentNetworkName,
        BigDecimal paymentEthAmount,
        String paymentExplorerUrl,
        Instant paidAtUtc,
        Instant createdAt,
        List<CartItemDto> items,
        List<TransparencyRecordDto> transparencyRecords) {
}

package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code DTOs/TransparencyRecordDto.cs}. */
public record TransparencyRecordDto(
        UUID id,
        UUID orderId,
        String orderNumber,
        String productName,
        int quantity,
        BigDecimal total,
        String orderHash,
        int chainId,
        String networkName,
        String contractAddress,
        String transactionHash,
        String explorerUrl,
        String status,
        Instant createdAt,
        Instant recordedOnChainAt) {
}

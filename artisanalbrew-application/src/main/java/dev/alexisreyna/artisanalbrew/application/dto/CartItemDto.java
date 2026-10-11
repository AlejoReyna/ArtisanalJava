package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Port of {@code DTOs/CartItemDto.cs}. */
public record CartItemDto(UUID productId, String productName, int quantity, BigDecimal unitPrice) {
}

package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;

import java.math.BigDecimal;
import java.util.UUID;

/** Port of {@code DTOs/ProductDto.cs}. */
public record ProductDto(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        ProductCategory category,
        boolean isActive,
        String origin,
        RoastLevel roastLevel,
        CoffeeProcess process) {
}

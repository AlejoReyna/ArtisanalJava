package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;

import java.math.BigDecimal;

/** Port of {@code DTOs/UpdateProductRequest.cs}. */
public record UpdateProductRequest(
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        ProductCategory category,
        String origin,
        RoastLevel roastLevel,
        CoffeeProcess process,
        boolean isActive) {
}

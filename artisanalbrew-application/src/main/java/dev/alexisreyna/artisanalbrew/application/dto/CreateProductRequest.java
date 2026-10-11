package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;

import java.math.BigDecimal;

/** Port of {@code DTOs/CreateProductRequest.cs}. */
public record CreateProductRequest(
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        String imageUrl,
        ProductCategory category,
        String origin,
        RoastLevel roastLevel,
        CoffeeProcess process) {
}

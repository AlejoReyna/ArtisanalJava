package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateProductRequest;

import java.math.BigDecimal;

/** Port of {@code CreateProductRequestValidator} + {@code UpdateProductRequestValidator}. */
public final class ProductValidators {
    private ProductValidators() {
    }

    public static void validateCreate(CreateProductRequest request) {
        require(request != null, "Request is required.");
        validateShared(request.name(), request.description(), request.price(), request.stockQuantity(),
                request.imageUrl(), request.origin());
        require(request.category() != null, "Category is required.");
    }

    public static void validateUpdate(UpdateProductRequest request) {
        require(request != null, "Request is required.");
        validateShared(request.name(), request.description(), request.price(), request.stockQuantity(),
                request.imageUrl(), request.origin());
        require(request.category() != null, "Category is required.");
    }

    private static void validateShared(String name, String description, BigDecimal price,
                                       int stockQuantity, String imageUrl, String origin) {
        require(name != null && !name.isEmpty() && name.length() <= 160, "Name must be 1-160 characters.");
        require(description != null && !description.isEmpty() && description.length() <= 1_000,
                "Description must be 1-1000 characters.");
        require(price != null && price.signum() > 0, "Price must be greater than 0.");
        require(stockQuantity >= 0, "StockQuantity must be >= 0.");
        require(imageUrl == null || imageUrl.length() <= 2_048, "ImageUrl must be <= 2048 characters.");
        require(origin == null || origin.length() <= 120, "Origin must be <= 120 characters.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}

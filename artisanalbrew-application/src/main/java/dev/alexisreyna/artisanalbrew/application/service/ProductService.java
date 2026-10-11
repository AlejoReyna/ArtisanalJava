package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.dto.ProductDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateProductRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port of {@code Services/IProductService.cs}. */
public interface ProductService {
    List<ProductDto> getProducts();

    Optional<ProductDto> getById(UUID id);

    Optional<ProductDto> getBySlug(String slug);

    ProductDto create(CreateProductRequest request);

    Optional<ProductDto> update(UUID id, UpdateProductRequest request);

    boolean delete(UUID id);
}

package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.common.SlugGenerator;
import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.dto.ProductDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateProductRequest;
import dev.alexisreyna.artisanalbrew.application.repository.ProductRepository;
import dev.alexisreyna.artisanalbrew.application.validation.ProductValidators;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port of {@code ProductService} in {@code Services/ProductService.cs}.
 * Framework-free: constructor injection, no annotations (wiring is Phase 5).
 */
public final class DefaultProductService implements ProductService {
    private final ProductRepository products;

    public DefaultProductService(ProductRepository products) {
        this.products = products;
    }

    @Override
    public List<ProductDto> getProducts() {
        return products.getProducts().stream().map(DefaultProductService::map).toList();
    }

    @Override
    public Optional<ProductDto> getById(UUID id) {
        return products.getById(id).map(DefaultProductService::map);
    }

    @Override
    public Optional<ProductDto> getBySlug(String slug) {
        return products.getBySlug(slug).map(DefaultProductService::map);
    }

    @Override
    public ProductDto create(CreateProductRequest request) {
        ProductValidators.validateCreate(request);
        Product product = new Product();
        product.setName(request.name());
        product.setSlug(SlugGenerator.create(request.name()));
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        product.setCategory(request.category());
        product.setOrigin(request.origin());
        product.setRoastLevel(request.roastLevel());
        product.setProcess(request.process());
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        products.add(product);
        return map(product);
    }

    @Override
    public Optional<ProductDto> update(UUID id, UpdateProductRequest request) {
        ProductValidators.validateUpdate(request);
        Optional<Product> existing = products.getById(id);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Product product = existing.get();
        product.setName(request.name());
        product.setSlug(SlugGenerator.create(request.name()));
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        product.setCategory(request.category());
        product.setOrigin(request.origin());
        product.setRoastLevel(request.roastLevel());
        product.setProcess(request.process());
        product.setActive(request.isActive());
        product.setUpdatedAt(Instant.now());
        products.update(product);
        return Optional.of(map(product));
    }

    @Override
    public boolean delete(UUID id) {
        Optional<Product> existing = products.getById(id);
        if (existing.isEmpty()) {
            return false;
        }
        products.delete(existing.get());
        return true;
    }

    static ProductDto map(Product product) {
        return new ProductDto(
                product.getId(), product.getName(), product.getSlug(), product.getDescription(),
                product.getPrice(), product.getStockQuantity(), product.getImageUrl(),
                product.getCategory(), product.isActive(), product.getOrigin(),
                product.getRoastLevel(), product.getProcess());
    }
}

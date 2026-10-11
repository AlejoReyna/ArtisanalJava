package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port of {@code Repositories/IProductRepository.cs}.
 * C# {@code Task<T>} becomes blocking {@code T}: the Spring port serves these
 * from virtual threads (Phase 5) instead of {@code async/await}.
 */
public interface ProductRepository {
    List<Product> getProducts();

    Optional<Product> getById(UUID id);

    Optional<Product> getBySlug(String slug);

    void add(Product product);

    void update(Product product);

    void delete(Product product);
}

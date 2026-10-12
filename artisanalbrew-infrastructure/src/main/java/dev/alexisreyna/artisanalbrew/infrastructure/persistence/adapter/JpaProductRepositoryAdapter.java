package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.ProductRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ProductEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ProductJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA adapter for {@link ProductRepository}. Method-per-method port of {@code ProductRepository.cs}. */
@Repository
@Transactional
public class JpaProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository jpa;

    public JpaProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return jpa.findAll().stream().map(JpaProductRepositoryAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getById(UUID id) {
        return jpa.findById(id).map(JpaProductRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getBySlug(String slug) {
        return jpa.findBySlug(slug).map(JpaProductRepositoryAdapter::toDomain);
    }

    @Override
    public void add(Product product) {
        jpa.save(toEntity(product));
    }

    @Override
    public void update(Product product) {
        jpa.save(toEntity(product));
    }

    @Override
    public void delete(Product product) {
        jpa.deleteById(product.getId());
    }

    static ProductEntity toEntity(Product product) {
        ProductEntity e = new ProductEntity();
        e.id = product.getId();
        e.name = product.getName();
        e.slug = product.getSlug();
        e.description = product.getDescription();
        e.price = product.getPrice();
        e.stockQuantity = product.getStockQuantity();
        e.imageUrl = product.getImageUrl();
        e.category = product.getCategory();
        e.active = product.isActive();
        e.origin = product.getOrigin();
        e.roastLevel = product.getRoastLevel();
        e.process = product.getProcess();
        e.createdAt = product.getCreatedAt();
        e.updatedAt = product.getUpdatedAt();
        return e;
    }

    static Product toDomain(ProductEntity e) {
        Product product = new Product();
        product.setId(e.id);
        product.setName(e.name);
        product.setSlug(e.slug);
        product.setDescription(e.description);
        product.setPrice(e.price);
        product.setStockQuantity(e.stockQuantity);
        product.setImageUrl(e.imageUrl);
        product.setCategory(e.category);
        product.setActive(e.active);
        product.setOrigin(e.origin);
        product.setRoastLevel(e.roastLevel);
        product.setProcess(e.process);
        product.setCreatedAt(e.createdAt);
        product.setUpdatedAt(e.updatedAt);
        return product;
    }
}

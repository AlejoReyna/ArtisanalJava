package dev.alexisreyna.artisanalbrew.domain.entities;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/Product.cs}. */
public class Product {
    private UUID id = UUID.randomUUID();
    private String name = "";
    private String slug = "";
    private String description = "";
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private ProductCategory category;
    private String origin;
    private RoastLevel roastLevel;
    private CoffeeProcess process;
    private boolean active = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public RoastLevel getRoastLevel() { return roastLevel; }
    public void setRoastLevel(RoastLevel roastLevel) { this.roastLevel = roastLevel; }
    public CoffeeProcess getProcess() { return process; }
    public void setProcess(CoffeeProcess process) { this.process = process; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

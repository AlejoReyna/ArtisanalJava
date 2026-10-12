package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert.DotNetEnumConverters;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code Products}. Flat by design — associations live in adapters, not here. */
@Entity
@Table(name = "\"Products\"")
public class ProductEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"Name\"")
    public String name;

    @Column(name = "\"Slug\"")
    public String slug;

    @Column(name = "\"Description\"")
    public String description;

    @Column(name = "\"Price\"")
    public BigDecimal price;

    @Column(name = "\"StockQuantity\"")
    public int stockQuantity;

    @Column(name = "\"ImageUrl\"")
    public String imageUrl;

    @Column(name = "\"Category\"")
    @Convert(converter = DotNetEnumConverters.ProductCategoryConverter.class)
    public ProductCategory category;

    @Column(name = "\"IsActive\"")
    public boolean active;

    @Column(name = "\"Origin\"")
    public String origin;

    @Column(name = "\"RoastLevel\"")
    @Convert(converter = DotNetEnumConverters.RoastLevelConverter.class)
    public RoastLevel roastLevel;

    @Column(name = "\"Process\"")
    @Convert(converter = DotNetEnumConverters.CoffeeProcessConverter.class)
    public CoffeeProcess process;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"UpdatedAt\"")
    public Instant updatedAt;
}

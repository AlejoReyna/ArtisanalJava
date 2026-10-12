package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code Coupons}. */
@Entity
@Table(name = "\"Coupons\"")
public class CouponEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"Code\"")
    public String code;

    @Column(name = "\"NormalizedCode\"")
    public String normalizedCode;

    @Column(name = "\"DiscountPercent\"")
    public BigDecimal discountPercent;

    @Column(name = "\"MinimumOrderTotal\"")
    public BigDecimal minimumOrderTotal;

    @Column(name = "\"IsActive\"")
    public boolean active;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"UpdatedAt\"")
    public Instant updatedAt;
}

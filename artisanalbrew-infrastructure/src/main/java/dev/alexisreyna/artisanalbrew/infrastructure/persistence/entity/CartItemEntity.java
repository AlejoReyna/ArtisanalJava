package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** JPA row for {@code CartItems}. */
@Entity
@Table(name = "\"CartItems\"")
public class CartItemEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"CartId\"")
    public UUID cartId;

    @Column(name = "\"ProductId\"")
    public UUID productId;

    @Column(name = "\"ProductName\"")
    public String productName;

    @Column(name = "\"Quantity\"")
    public int quantity;

    @Column(name = "\"UnitPrice\"")
    public BigDecimal unitPrice;
}

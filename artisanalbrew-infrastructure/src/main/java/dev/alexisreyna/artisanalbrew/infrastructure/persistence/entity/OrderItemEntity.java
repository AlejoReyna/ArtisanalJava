package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/** JPA row for {@code OrderItems}. */
@Entity
@Table(name = "\"OrderItems\"")
public class OrderItemEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"OrderId\"")
    public UUID orderId;

    @Column(name = "\"ProductId\"")
    public UUID productId;

    @Column(name = "\"ProductName\"")
    public String productName;

    @Column(name = "\"Quantity\"")
    public int quantity;

    @Column(name = "\"UnitPrice\"")
    public BigDecimal unitPrice;

    @Column(name = "\"Total\"")
    public BigDecimal total;
}

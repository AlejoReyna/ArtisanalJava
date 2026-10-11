package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/CartItem.cs}. */
public class CartItem {
    private UUID id = UUID.randomUUID();
    private UUID cartId;
    private UUID productId;
    private String productName = "";
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}

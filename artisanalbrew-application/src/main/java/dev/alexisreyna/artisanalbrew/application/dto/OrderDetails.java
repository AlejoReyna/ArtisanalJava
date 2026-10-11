package dev.alexisreyna.artisanalbrew.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Port of {@code OrderDetails} + {@code OrderItemDetails} in {@code DTOs/OrderDetails.cs}. */
public final class OrderDetails {
    private String orderId;
    private String customerEmail;
    private String customerName;
    private OffsetDateTime purchaseDate;
    private List<OrderItemDetails> items;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal shipping = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private String couponCode;
    private BigDecimal couponDiscountPercent;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public String getOrderId() { return orderId; }
    public void setOrderId(String v) { this.orderId = v; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String v) { this.customerEmail = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { this.customerName = v; }
    public OffsetDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(OffsetDateTime v) { this.purchaseDate = v; }
    public List<OrderItemDetails> getItems() { return items; }
    public void setItems(List<OrderItemDetails> v) { this.items = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { this.subtotal = v; }
    public BigDecimal getShipping() { return shipping; }
    public void setShipping(BigDecimal v) { this.shipping = v; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal v) { this.tax = v; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String v) { this.couponCode = v; }
    public BigDecimal getCouponDiscountPercent() { return couponDiscountPercent; }
    public void setCouponDiscountPercent(BigDecimal v) { this.couponDiscountPercent = v; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal v) { this.discountAmount = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { this.total = v; }

    public static final class OrderItemDetails {
        private String name;
        private int qty;
        private BigDecimal price = BigDecimal.ZERO;

        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public int getQty() { return qty; }
        public void setQty(int v) { this.qty = v; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal v) { this.price = v; }
    }
}

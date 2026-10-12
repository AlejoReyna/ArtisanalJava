package dev.alexisreyna.artisanalbrew.domain.entities;

import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Port of {@code ThisCafeteria.Domain/Entities/Order.cs}.
 *
 * <p>Note the .NET quirk preserved here: {@link #place} creates the order directly in
 * {@code PROCESSING} status, while {@link #transitionTo} only allows
 * {@code PENDING -> PROCESSING|CANCELLED} and {@code PROCESSING -> COMPLETED|CANCELLED}.</p>
 */
public class Order {
    private UUID id = UUID.randomUUID();
    private String orderNumber = "";
    private UUID userProfileId;
    private OrderStatus status = OrderStatus.PENDING;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal tax;
    private UUID couponId;
    private String couponCode;
    private BigDecimal couponDiscountPercent;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String walletAddress = "";
    private String paymentTransactionHash;
    private Integer paymentChainId;
    private String paymentNetworkName;
    private BigDecimal paymentEthAmount;
    private String paymentExplorerUrl;
    private Instant paidAtUtc;
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
    private List<OrderItem> items = new ArrayList<>();
    private List<TransparencyRecord> transparencyRecords = new ArrayList<>();

    public static Order place(UUID userProfileId, String orderNumber, String walletAddress,
                              List<OrderItem> items, OrderPricing pricing, Coupon coupon) {
        return place(userProfileId, orderNumber, walletAddress, items, pricing, coupon, null);
    }

    public static Order place(UUID userProfileId, String orderNumber, String walletAddress,
                              List<OrderItem> items, OrderPricing pricing, Coupon coupon, Instant createdAtUtc) {
        if (userProfileId == null || orderNumber == null || orderNumber.isBlank()
                || walletAddress == null || walletAddress.isBlank()) {
            throw new IllegalStateException("Order identity is required.");
        }
        Order order = new Order();
        order.userProfileId = userProfileId;
        order.orderNumber = orderNumber;
        order.walletAddress = walletAddress;
        order.status = OrderStatus.PROCESSING;
        order.subtotal = pricing.subtotal();
        order.shipping = pricing.shipping();
        order.tax = pricing.tax();
        order.discountAmount = pricing.discountAmount();
        order.total = pricing.total();
        if (coupon != null) {
            order.couponId = coupon.getId();
            order.couponCode = coupon.getCode();
            order.couponDiscountPercent = coupon.getDiscountPercent();
        }
        order.createdAt = createdAtUtc != null ? createdAtUtc : Instant.now();
        order.items = new ArrayList<>(items);
        return order;
    }

    public void recordPayment(String transactionHash, Integer chainId, String networkName,
                              BigDecimal ethAmount, String explorerUrl, Instant paidAtUtc) {
        this.paymentTransactionHash = transactionHash;
        this.paymentChainId = chainId;
        this.paymentNetworkName = networkName;
        this.paymentEthAmount = ethAmount;
        this.paymentExplorerUrl = explorerUrl;
        this.paidAtUtc = paidAtUtc;
        this.updatedAt = Instant.now();
    }

    /**
     * Rebuilds an order from persisted state (see {@code Coupon#rehydrate} for
     * why this exists: EF Core rehydrates through private setters, Java
     * through an explicit factory). No validation on read.
     */
    public static Order rehydrate(UUID id, String orderNumber, UUID userProfileId, OrderStatus status,
                                  BigDecimal subtotal, BigDecimal shipping, BigDecimal tax,
                                  UUID couponId, String couponCode, BigDecimal couponDiscountPercent,
                                  BigDecimal discountAmount, BigDecimal total, String walletAddress,
                                  String paymentTransactionHash, Integer paymentChainId, String paymentNetworkName,
                                  BigDecimal paymentEthAmount, String paymentExplorerUrl, Instant paidAtUtc,
                                  Instant createdAt, Instant updatedAt,
                                  List<OrderItem> items, List<TransparencyRecord> transparencyRecords) {
        Order order = new Order();
        order.id = id;
        order.orderNumber = orderNumber;
        order.userProfileId = userProfileId;
        order.status = status;
        order.subtotal = subtotal;
        order.shipping = shipping;
        order.tax = tax;
        order.couponId = couponId;
        order.couponCode = couponCode;
        order.couponDiscountPercent = couponDiscountPercent;
        order.discountAmount = discountAmount;
        order.total = total;
        order.walletAddress = walletAddress;
        order.paymentTransactionHash = paymentTransactionHash;
        order.paymentChainId = paymentChainId;
        order.paymentNetworkName = paymentNetworkName;
        order.paymentEthAmount = paymentEthAmount;
        order.paymentExplorerUrl = paymentExplorerUrl;
        order.paidAtUtc = paidAtUtc;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        order.items = new ArrayList<>(items);
        order.transparencyRecords = new ArrayList<>(transparencyRecords);
        return order;
    }

    public void transitionTo(OrderStatus nextStatus) {
        boolean allowed = switch (status) {
            case PENDING -> nextStatus == OrderStatus.PROCESSING || nextStatus == OrderStatus.CANCELLED;
            case PROCESSING -> nextStatus == OrderStatus.COMPLETED || nextStatus == OrderStatus.CANCELLED;
            default -> false;
        };
        if (!allowed) {
            throw new IllegalStateException("Cannot transition order from " + status + " to " + nextStatus + ".");
        }
        this.status = nextStatus;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getUserProfileId() { return userProfileId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getShipping() { return shipping; }
    public BigDecimal getTax() { return tax; }
    public UUID getCouponId() { return couponId; }
    public String getCouponCode() { return couponCode; }
    public BigDecimal getCouponDiscountPercent() { return couponDiscountPercent; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotal() { return total; }
    public String getWalletAddress() { return walletAddress; }
    public String getPaymentTransactionHash() { return paymentTransactionHash; }
    public Integer getPaymentChainId() { return paymentChainId; }
    public String getPaymentNetworkName() { return paymentNetworkName; }
    public BigDecimal getPaymentEthAmount() { return paymentEthAmount; }
    public String getPaymentExplorerUrl() { return paymentExplorerUrl; }
    public Instant getPaidAtUtc() { return paidAtUtc; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return items; }
    public List<TransparencyRecord> getTransparencyRecords() { return transparencyRecords; }
}

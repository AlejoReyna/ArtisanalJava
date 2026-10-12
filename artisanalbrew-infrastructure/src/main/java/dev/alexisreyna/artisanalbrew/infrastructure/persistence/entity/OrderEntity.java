package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert.DotNetEnumConverters;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code Orders}. */
@Entity
@Table(name = "\"Orders\"")
public class OrderEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"OrderNumber\"")
    public String orderNumber;

    @Column(name = "\"UserProfileId\"")
    public UUID userProfileId;

    @Column(name = "\"Status\"")
    @Convert(converter = DotNetEnumConverters.OrderStatusConverter.class)
    public OrderStatus status;

    @Column(name = "\"Subtotal\"")
    public BigDecimal subtotal;

    @Column(name = "\"Shipping\"")
    public BigDecimal shipping;

    @Column(name = "\"Tax\"")
    public BigDecimal tax;

    @Column(name = "\"CouponId\"")
    public UUID couponId;

    @Column(name = "\"CouponCode\"")
    public String couponCode;

    @Column(name = "\"CouponDiscountPercent\"")
    public BigDecimal couponDiscountPercent;

    @Column(name = "\"DiscountAmount\"")
    public BigDecimal discountAmount;

    @Column(name = "\"Total\"")
    public BigDecimal total;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"PaymentTransactionHash\"")
    public String paymentTransactionHash;

    @Column(name = "\"PaymentChainId\"")
    public Integer paymentChainId;

    @Column(name = "\"PaymentNetworkName\"")
    public String paymentNetworkName;

    @Column(name = "\"PaymentEthAmount\"")
    public BigDecimal paymentEthAmount;

    @Column(name = "\"PaymentExplorerUrl\"")
    public String paymentExplorerUrl;

    @Column(name = "\"PaidAtUtc\"")
    public Instant paidAtUtc;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"UpdatedAt\"")
    public Instant updatedAt;
}

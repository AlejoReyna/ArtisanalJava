package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.OrderRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.CouponRedemption;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderItem;
import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CouponRedemptionEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.OrderEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.OrderItemEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponRedemptionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.TransparencyRecordJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for {@link OrderRepository}. Persists the aggregate explicitly
 * (order → items → redemption), mirroring EF cascade timing: one transaction,
 * same commit.
 */
@Repository
@Transactional
public class JpaOrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository orders;
    private final OrderItemJpaRepository items;
    private final CouponRedemptionJpaRepository redemptions;
    private final TransparencyRecordJpaRepository transparency;

    public JpaOrderRepositoryAdapter(OrderJpaRepository orders, OrderItemJpaRepository items,
                                     CouponRedemptionJpaRepository redemptions,
                                     TransparencyRecordJpaRepository transparency) {
        this.orders = orders;
        this.items = items;
        this.redemptions = redemptions;
        this.transparency = transparency;
    }

    @Override
    public void add(Order order) {
        orders.save(toEntity(order));
        items.saveAll(order.getItems().stream().map(item -> toItemEntity(order.getId(), item)).toList());
        if (order.getCouponId() != null) {
            CouponRedemptionEntity redemption = new CouponRedemptionEntity();
            redemption.id = UUID.randomUUID();
            redemption.couponId = order.getCouponId();
            redemption.userProfileId = order.getUserProfileId();
            redemption.orderId = order.getId();
            redemption.redeemedAtUtc = Instant.now();
            redemptions.save(redemption);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPaymentHash(String paymentTransactionHash) {
        if (paymentTransactionHash == null) {
            return false;
        }
        return orders.existsByPaymentTransactionHash(paymentTransactionHash);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getById(UUID id) {
        return orders.findById(id).map(order -> assemble(order,
                items.findByOrderId(id), transparency.findByOrderId(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getCommerceTransactions() {
        return orders.findAll().stream().map(order -> assemble(order,
                items.findByOrderId(order.id), transparency.findByOrderId(order.id))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(UUID userProfileId) {
        return orders.findByUserProfileId(userProfileId).stream().map(order -> assemble(order,
                items.findByOrderId(order.id), transparency.findByOrderId(order.id))).toList();
    }

    @Override
    public void delete(Order order) {
        redemptions.deleteByOrderId(order.getId());
        transparency.deleteByOrderId(order.getId());
        items.deleteByOrderId(order.getId());
        orders.deleteById(order.getId());
    }

    static OrderEntity toEntity(Order order) {
        OrderEntity e = new OrderEntity();
        e.id = order.getId();
        e.orderNumber = order.getOrderNumber();
        e.userProfileId = order.getUserProfileId();
        e.status = order.getStatus();
        e.subtotal = order.getSubtotal();
        e.shipping = order.getShipping();
        e.tax = order.getTax();
        e.couponId = order.getCouponId();
        e.couponCode = order.getCouponCode();
        e.couponDiscountPercent = order.getCouponDiscountPercent();
        e.discountAmount = order.getDiscountAmount();
        e.total = order.getTotal();
        e.walletAddress = order.getWalletAddress();
        e.paymentTransactionHash = order.getPaymentTransactionHash();
        e.paymentChainId = order.getPaymentChainId();
        e.paymentNetworkName = order.getPaymentNetworkName();
        e.paymentEthAmount = order.getPaymentEthAmount();
        e.paymentExplorerUrl = order.getPaymentExplorerUrl();
        e.paidAtUtc = order.getPaidAtUtc();
        e.createdAt = order.getCreatedAt();
        e.updatedAt = order.getUpdatedAt();
        return e;
    }

    static OrderItemEntity toItemEntity(UUID orderId, OrderItem item) {
        OrderItemEntity e = new OrderItemEntity();
        e.id = item.getId();
        e.orderId = orderId;
        e.productId = item.getProductId();
        e.productName = item.getProductName();
        e.quantity = item.getQuantity();
        e.unitPrice = item.getUnitPrice();
        e.total = item.getTotal();
        return e;
    }

    static Order assemble(OrderEntity e, List<OrderItemEntity> itemRows,
                          List<dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.TransparencyRecordEntity> recordRows) {
        List<OrderItem> orderItems = itemRows.stream().map(row -> {
            OrderItem item = new OrderItem();
            item.setId(row.id);
            item.setOrderId(row.orderId);
            item.setProductId(row.productId);
            item.setProductName(row.productName);
            item.setQuantity(row.quantity);
            item.setUnitPrice(row.unitPrice);
            item.setTotal(row.total);
            return item;
        }).toList();
        List<TransparencyRecord> records = recordRows.stream().map(row -> {
            TransparencyRecord record = new TransparencyRecord();
            record.setId(row.id);
            record.setOrderId(row.orderId);
            record.setOrderNumber(row.orderNumber);
            record.setProductName(row.productName);
            record.setQuantity(row.quantity);
            record.setTotal(row.total);
            record.setOrderHash(row.orderHash);
            record.setChainId(row.chainId);
            record.setNetworkName(row.networkName);
            record.setContractAddress(row.contractAddress);
            record.setTransactionHash(row.transactionHash);
            record.setExplorerUrl(row.explorerUrl);
            record.setStatus(row.status);
            record.setCreatedAt(row.createdAt);
            record.setRecordedOnChainAt(row.recordedOnChainAt);
            return record;
        }).toList();
        return Order.rehydrate(e.id, e.orderNumber, e.userProfileId, e.status,
                e.subtotal, e.shipping, e.tax, e.couponId, e.couponCode, e.couponDiscountPercent,
                e.discountAmount, e.total, e.walletAddress, e.paymentTransactionHash, e.paymentChainId,
                e.paymentNetworkName, e.paymentEthAmount, e.paymentExplorerUrl, e.paidAtUtc,
                e.createdAt, e.updatedAt, orderItems, records);
    }

    static CouponRedemption toRedemption(CouponRedemptionEntity e) {
        CouponRedemption redemption = new CouponRedemption();
        redemption.setId(e.id);
        redemption.setCouponId(e.couponId);
        redemption.setUserProfileId(e.userProfileId);
        redemption.setOrderId(e.orderId);
        redemption.setRedeemedAtUtc(e.redeemedAtUtc);
        return redemption;
    }
}

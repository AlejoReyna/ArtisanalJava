package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CommerceTransactionDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.dto.OrderDto;
import dev.alexisreyna.artisanalbrew.application.dto.OrderPricingDto;
import dev.alexisreyna.artisanalbrew.application.dto.TransparencyRecordDto;
import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.application.repository.OrderRepository;
import dev.alexisreyna.artisanalbrew.application.repository.ProductRepository;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.validation.OrderValidators;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;
import dev.alexisreyna.artisanalbrew.domain.entities.CouponRedemption;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderItem;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderPricing;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Port of {@code OrderService} in {@code Services/OrderService.cs}.
 * Flow preserved: validate → auth → resolve chain → requote from catalog →
 * coupon → price → native amount → payment-hash guard → on-chain verify →
 * decrement stock → place → record payment → persist → transparency.
 */
public final class DefaultOrderService implements OrderService {
    private static final DateTimeFormatter ORDER_NUMBER_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final OrderRepository orders;
    private final ProductRepository products;
    private final CouponRepository coupons;
    private final OrderPricingService pricing;
    private final MarketplacePaymentGateway paymentGateway;
    private final ChainRegistry chains;
    private final TransparencyService transparency;

    public DefaultOrderService(OrderRepository orders, ProductRepository products, CouponRepository coupons,
                               OrderPricingService pricing, MarketplacePaymentGateway paymentGateway,
                               ChainRegistry chains, TransparencyService transparency) {
        this.orders = orders;
        this.products = products;
        this.coupons = coupons;
        this.pricing = pricing;
        this.paymentGateway = paymentGateway;
        this.chains = chains;
        this.transparency = transparency;
    }

    @Override
    public OrderDto createOrder(CreateOrderRequest request, UUID authenticatedUserProfileId) {
        OrderValidators.validateCreate(request);
        if (authenticatedUserProfileId == null) {
            throw new IllegalStateException("Sign in before placing an order.");
        }
        ChainDefinition chain = resolveMarketplaceChain(request.paymentChainId());
        List<CartItemDto> catalogItems = requoteFromCatalog(request.items());
        Coupon coupon = resolveCoupon(catalogItems, request.couponCode(), authenticatedUserProfileId);
        OrderPricingDto quoted = pricing.calculate(catalogItems, coupon);
        BigDecimal expectedNativeAmount = pricing.toNativePaymentAmount(quoted.total(), chain.getNativeCurrencySymbol());

        if (orders.existsByPaymentHash(request.paymentTransactionHash())) {
            throw new IllegalStateException("This payment has already been used for an order.");
        }
        StakingVerificationResult verification = paymentGateway.verifyNativePayment(
                chain.getKey(), request.paymentTransactionHash(), request.walletAddress(), expectedNativeAmount);
        if (verification.status() == TransactionVerificationStatus.PENDING_CONFIRMATIONS) {
            throw new IllegalStateException("This payment is still confirming. Try again in a few seconds.");
        }
        if (verification.status() != TransactionVerificationStatus.VERIFIED) {
            throw new IllegalStateException("This payment could not be verified on-chain.");
        }

        decrementStock(catalogItems);

        List<OrderItem> items = catalogItems.stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.productId());
            orderItem.setProductName(item.productName());
            orderItem.setQuantity(item.quantity());
            orderItem.setUnitPrice(item.unitPrice());
            orderItem.setTotal(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
            return orderItem;
        }).toList();
        Order order = Order.place(
                authenticatedUserProfileId,
                "TC-" + ORDER_NUMBER_TIME.format(Instant.now()) + "-"
                        + ThreadLocalRandom.current().nextInt(1000, 9999),
                request.walletAddress(),
                items,
                new OrderPricing(quoted.subtotal(), quoted.shipping(), quoted.tax(),
                        quoted.totalBeforeDiscount(), quoted.discountAmount(), quoted.total()),
                coupon);
        order.recordPayment(
                request.paymentTransactionHash(),
                chain.getEvmChainId(),
                chain.getDisplayName(),
                expectedNativeAmount,
                chain.getExplorerTransactionTemplate().replace("{0}", request.paymentTransactionHash()),
                Instant.now());

        // No redemption entity is created here: like the .NET flow (which attaches it
        // to Order.CouponRedemption for EF cascade), persistence derives the
        // CouponRedemption row from order.couponId/userProfileId in Phase 3.

        orders.add(order);
        transparency.createPendingRecordsForOrder(order);
        return map(order);
    }

    @Override
    public List<OrderDto> getOrdersForUser(UUID userProfileId) {
        return orders.getOrdersForUser(userProfileId).stream().map(DefaultOrderService::map).toList();
    }

    @Override
    public List<CommerceTransactionDto> getCommerceTransactions() {
        return orders.getCommerceTransactions().stream().map(DefaultOrderService::mapCommerceTransaction).toList();
    }

    @Override
    public boolean deleteOrder(UUID id) {
        return orders.getById(id).map(order -> {
            orders.delete(order);
            return true;
        }).orElse(false);
    }

    private ChainDefinition resolveMarketplaceChain(int paymentChainId) {
        ChainDefinition chain = chains.all().stream()
                .filter(c -> c.isEnabled() && c.getFamily() == ChainFamily.EVM
                        && c.getEvmChainId() != null && c.getEvmChainId() == paymentChainId)
                .findFirst()
                .orElse(null);
        if (chain == null || !chain.getCapabilities().marketplacePayment()) {
            throw new IllegalStateException("Checkout is not available on the selected network.");
        }
        return chain;
    }

    private List<CartItemDto> requoteFromCatalog(List<CartItemDto> requestedItems) {
        List<CartItemDto> quoted = new ArrayList<>(requestedItems.size());
        for (CartItemDto requested : requestedItems) {
            Product product = products.getById(requested.productId()).orElse(null);
            if (product == null || !product.isActive()) {
                throw new IllegalStateException("Product '" + requested.productName() + "' is no longer available.");
            }
            if (product.getStockQuantity() < requested.quantity()) {
                throw new IllegalStateException("Only " + product.getStockQuantity()
                        + " unit(s) of '" + product.getName() + "' are available.");
            }
            quoted.add(new CartItemDto(product.getId(), product.getName(), requested.quantity(), product.getPrice()));
        }
        return quoted;
    }

    private void decrementStock(List<CartItemDto> items) {
        for (CartItemDto item : items) {
            Product product = products.getById(item.productId()).orElse(null);
            if (product == null || product.getStockQuantity() < item.quantity()) {
                throw new IllegalStateException("'" + item.productName()
                        + "' went out of stock before the order could be recorded.");
            }
            product.setStockQuantity(product.getStockQuantity() - item.quantity());
            product.setUpdatedAt(Instant.now());
            products.update(product);
        }
    }

    private Coupon resolveCoupon(List<CartItemDto> items, String couponCode, UUID userProfileId) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        Coupon coupon = coupons.getByNormalizedCode(Coupon.normalizeCode(couponCode)).orElse(null);
        if (coupon == null || !coupon.isActive()) {
            throw new IllegalStateException("Coupon code is invalid or inactive.");
        }
        OrderPricingDto quote = pricing.calculate(items);
        if (!coupon.canBeRedeemedFor(quote.totalBeforeDiscount())) {
            throw new IllegalStateException(
                    "Coupon requires a minimum order total of " + coupon.getMinimumOrderTotal() + ".");
        }
        if (coupons.hasUserRedeemed(coupon.getId(), userProfileId)) {
            throw new IllegalStateException("You have already redeemed this coupon.");
        }
        return coupon;
    }

    static OrderDto map(Order order) {
        return new OrderDto(
                order.getId(), order.getOrderNumber(), order.getUserProfileId(), order.getStatus(),
                order.getSubtotal(), order.getShipping(), order.getTax(),
                order.getCouponCode(), order.getCouponDiscountPercent(), order.getDiscountAmount(), order.getTotal(),
                order.getWalletAddress(), order.getPaymentTransactionHash(), order.getPaymentChainId(),
                order.getPaymentNetworkName(), order.getPaymentEthAmount(), order.getPaymentExplorerUrl(),
                order.getPaidAtUtc(), order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new CartItemDto(item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                order.getTransparencyRecords().stream()
                        .map(record -> new TransparencyRecordDto(
                                record.getId(), record.getOrderId(), record.getOrderNumber(),
                                record.getProductName(), record.getQuantity(), record.getTotal(),
                                record.getOrderHash(), record.getChainId(), record.getNetworkName(),
                                record.getContractAddress(), record.getTransactionHash(), record.getExplorerUrl(),
                                record.getStatus(), record.getCreatedAt(), record.getRecordedOnChainAt()))
                        .toList());
    }

    static CommerceTransactionDto mapCommerceTransaction(Order order) {
        return new CommerceTransactionDto(
                order.getId(), order.getOrderNumber(), order.getStatus(), order.getTotal(),
                order.getWalletAddress(), order.getPaymentTransactionHash(), order.getPaymentChainId(),
                order.getPaymentNetworkName(), order.getPaymentEthAmount(), order.getPaymentExplorerUrl(),
                order.getPaidAtUtc(), order.getCreatedAt(),
                order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
                buildProductSummary(order.getItems()));
    }

    private static String buildProductSummary(List<OrderItem> items) {
        if (items.isEmpty()) {
            return "No items";
        }
        return items.stream()
                .sorted(Comparator.comparing(OrderItem::getProductName))
                .map(item -> item.getQuantity() > 1
                        ? item.getProductName() + " x" + item.getQuantity()
                        : item.getProductName())
                .collect(Collectors.joining(", "));
    }
}

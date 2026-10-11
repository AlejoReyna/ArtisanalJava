package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainCapabilities;
import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.DefaultChainRegistry;
import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.application.repository.OrderRepository;
import dev.alexisreyna.artisanalbrew.application.repository.TransparencyRecordRepository;
import dev.alexisreyna.artisanalbrew.application.service.ProductCouponServiceTest.FakeCoupons;
import dev.alexisreyna.artisanalbrew.application.service.ProductCouponServiceTest.FakeProducts;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.domain.entities.Coupon;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;
import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultOrderServiceTest {

    static final class FakeOrders implements OrderRepository {
        final Map<UUID, Order> store = new HashMap<>();

        @Override
        public void add(Order order) {
            store.put(order.getId(), order);
        }

        @Override
        public boolean existsByPaymentHash(String hash) {
            return store.values().stream().anyMatch(o -> hash.equals(o.getPaymentTransactionHash()));
        }

        @Override
        public Optional<Order> getById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Order> getCommerceTransactions() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Order> getOrdersForUser(UUID userProfileId) {
            return store.values().stream().filter(o -> o.getUserProfileId().equals(userProfileId)).toList();
        }

        @Override
        public void delete(Order order) {
            store.remove(order.getId());
        }
    }

    static final class FakeTransparency implements TransparencyRecordRepository {
        final List<TransparencyRecord> rows = new ArrayList<>();

        @Override
        public void addRange(List<TransparencyRecord> records) {
            rows.addAll(records);
        }

        @Override
        public List<TransparencyRecord> getRecent(int count) {
            return rows.stream().limit(count).toList();
        }
    }

    static final class VerifiedGateway implements MarketplacePaymentGateway {
        @Override
        public StakingVerificationResult verifyNativePayment(String chainKey, String transactionHash,
                                                             String expectedFromWallet, BigDecimal expectedAmount) {
            return new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, expectedAmount, 2, 2);
        }
    }

    private static final String WALLET = "0x" + "ab".repeat(20);
    private static final String TX_HASH = "0x" + "cd".repeat(32);

    private Fixture fixture() {
        FakeProducts products = new FakeProducts();
        FakeCoupons coupons = new FakeCoupons();
        FakeOrders orders = new FakeOrders();
        FakeTransparency transparency = new FakeTransparency();
        Product product = new Product();
        product.setName("Espresso");
        product.setSlug("espresso");
        product.setDescription("Strong");
        product.setPrice(new BigDecimal("10.00"));
        product.setStockQuantity(10);
        product.setActive(true);
        products.add(product);

        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setEvmChainId(11155111);
        chain.setEvmChainIdHex("0xaa36a7");
        chain.setNativeCurrencySymbol("ETH");
        chain.setPublicRpcUrl("https://ethereum-sepolia-rpc.publicnode.com");
        chain.setExplorerTransactionTemplate("https://sepolia.etherscan.io/tx/{0}");
        chain.setSortOrder(1);
        chain.getDeployment().setLegacyPool("0x932d50E20F917B9BbBe2C40F30D43BCef0e93F90");
        chain.setCapabilities(new ChainCapabilities(true, false, false, false, true, false, false, false));
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey("ethereum-sepolia");
        options.setChains(List.of(chain));

        BlockchainNetworkOptions network = new BlockchainNetworkOptions();
        DefaultOrderService service = new DefaultOrderService(orders, products, coupons,
                new DefaultOrderPricingService(), new VerifiedGateway(),
                new DefaultChainRegistry(options),
                new DefaultTransparencyService(transparency, network));
        return new Fixture(service, orders, products, coupons, transparency, product);
    }

    private record Fixture(DefaultOrderService service, FakeOrders orders, FakeProducts products,
                           CouponRepository coupons, FakeTransparency transparency, Product product) {
    }

    private CreateOrderRequest request(UUID productId, String txHash, String couponCode) {
        return new CreateOrderRequest(UUID.randomUUID(),
                List.of(new CartItemDto(productId, "Espresso", 2, new BigDecimal("999.00"))),
                WALLET, txHash, 11155111, "Ethereum Sepolia", new BigDecimal("0.01"),
                "https://sepolia.etherscan.io/tx/" + txHash, Instant.now(), couponCode);
    }

    @Test
    void happyPathRequotesVerifiesDecrementsAndPersists() {
        Fixture f = fixture();
        var dto = f.service().createOrder(request(f.product().getId(), TX_HASH, null), UUID.randomUUID());
        assertThat(dto.orderNumber()).startsWith("TC-");
        // Client price ignored: catalog price wins.
        assertThat(dto.subtotal()).isEqualByComparingTo("20.00");
        assertThat(dto.paymentTransactionHash()).isEqualTo(TX_HASH);
        assertThat(dto.paymentExplorerUrl()).isEqualTo("https://sepolia.etherscan.io/tx/" + TX_HASH);
        assertThat(f.products().getById(f.product().getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
        assertThat(dto.transparencyRecords()).hasSize(1);
        assertThat(dto.transparencyRecords().get(0).orderHash()).startsWith("0x");
        assertThat(f.service().getOrdersForUser(dto.userProfileId())).hasSize(1);
        assertThat(f.service().getCommerceTransactions()).hasSize(1);
        assertThat(f.service().deleteOrder(dto.id())).isTrue();
        assertThat(f.service().deleteOrder(dto.id())).isFalse();
    }

    @Test
    void duplicatePaymentHashRejected() {
        Fixture f = fixture();
        UUID user = UUID.randomUUID();
        f.service().createOrder(request(f.product().getId(), TX_HASH, null), user);
        assertThatThrownBy(() -> f.service().createOrder(request(f.product().getId(), TX_HASH, null), user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void inactiveProductAndShortStockRejected() {
        Fixture f = fixture();
        f.product().setActive(false);
        assertThatThrownBy(() -> f.service().createOrder(request(f.product().getId(), TX_HASH, null), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer available");
        f.product().setActive(true);
        f.product().setStockQuantity(1);
        assertThatThrownBy(() -> f.service().createOrder(request(f.product().getId(), TX_HASH, null), UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only 1 unit");
    }

    @Test
    void couponDiscountApplied() {
        Fixture f = fixture();
        Coupon coupon = Coupon.create("SAVE10", new BigDecimal("50"), BigDecimal.ZERO);
        f.coupons().add(coupon);
        var dto = f.service().createOrder(request(f.product().getId(), TX_HASH, "save10"), UUID.randomUUID());
        assertThat(dto.couponCode()).isEqualTo("SAVE10");
        assertThat(dto.discountAmount()).isPositive();
        assertThat(dto.total()).isLessThan(dto.subtotal().add(dto.shipping()).add(dto.tax()));
    }
}

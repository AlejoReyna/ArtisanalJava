package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainCapabilities;
import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.DefaultChainRegistry;
import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.dto.CreateProductRequest;
import dev.alexisreyna.artisanalbrew.application.service.DefaultCouponService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultOrderPricingService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultOrderService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultTransparencyService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.domain.entities.Product;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaCouponRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaOrderRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaProductRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaTransparencyRecordRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponRedemptionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserProfileEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ProductJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.TransparencyRecordJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end commerce flow through the REAL use-case services over JPA
 * adapters on a migrated database: catalog → order → stock → transparency.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class ProductOrderFlowTest {

    @Autowired
    private ProductJpaRepository productJpa;
    @Autowired
    private CouponJpaRepository couponJpa;
    @Autowired
    private CouponRedemptionJpaRepository redemptionJpa;
    @Autowired
    private OrderJpaRepository orderJpa;
    @Autowired
    private OrderItemJpaRepository orderItemJpa;
    @Autowired
    private TransparencyRecordJpaRepository transparencyJpa;
    @Autowired
    private UserProfileJpaRepository profileJpa;

    private static final String WALLET = "0x" + "ab".repeat(20);
    private static final String TX = "0x" + "cd".repeat(32);

    private UUID existingUser() {
        UserProfileEntity profile = new UserProfileEntity();
        profile.id = UUID.randomUUID();
        profile.email = "buyer@example.com";
        profile.displayName = "Buyer";
        profile.role = dev.alexisreyna.artisanalbrew.domain.enums.UserRole.CUSTOMER;
        profile.createdAt = Instant.now();
        profileJpa.save(profile);
        return profile.id;
    }

    private DefaultOrderService orders() {        var products = new JpaProductRepositoryAdapter(productJpa);
        var coupons = new JpaCouponRepositoryAdapter(couponJpa, redemptionJpa);
        var orderRepo = new JpaOrderRepositoryAdapter(orderJpa, orderItemJpa, redemptionJpa, transparencyJpa);
        var transparency = new JpaTransparencyRecordRepositoryAdapter(transparencyJpa);
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
        chain.getDeployment().setLegacyPool("0xpool");
        chain.setCapabilities(new ChainCapabilities(true, false, false, false, true, false, false, false));
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey("ethereum-sepolia");
        options.setChains(List.of(chain));
        MarketplacePaymentGateway gateway = (chainKey, hash, from, amount) ->
                new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, amount, 2, 2);
        return new DefaultOrderService(orderRepo, products, coupons, new DefaultOrderPricingService(),
                gateway, new DefaultChainRegistry(options),
                new DefaultTransparencyService(transparency, new BlockchainNetworkOptions()));
    }

    @Test
    void catalogToPaidOrder() {
        var products = new JpaProductRepositoryAdapter(productJpa);
        var catalog = new dev.alexisreyna.artisanalbrew.application.service.DefaultProductService(products);
        var created = catalog.create(new CreateProductRequest("Test Espresso", "Desc",
                new BigDecimal("10.00"), 10, null, ProductCategory.BEANS, null, null, null));
        UUID user = existingUser();
        var dto = orders().createOrder(new CreateOrderRequest(user,
                List.of(new CartItemDto(created.id(), "Test Espresso", 2, BigDecimal.ONE)),
                WALLET, TX, 11155111, "Ethereum Sepolia", new BigDecimal("0.01"),
                "https://sepolia.etherscan.io/tx/" + TX, Instant.now()), user);

        assertThat(dto.subtotal()).isEqualByComparingTo("20.00");
        assertThat(products.getById(created.id()).orElseThrow().getStockQuantity()).isEqualTo(8);
        assertThat(dto.transparencyRecords()).hasSize(1);
        assertThatThrownBy(() -> orders().createOrder(new CreateOrderRequest(user,
                List.of(new CartItemDto(created.id(), "Test Espresso", 1, BigDecimal.ONE)),
                WALLET, TX, 11155111, "Ethereum Sepolia", new BigDecimal("0.01"),
                "https://sepolia.etherscan.io/tx/" + TX, Instant.now()), user))
                .hasMessageContaining("already been used");
    }

    @Test
    void seededCatalogSurvivesRoundTrip() {
        Product seed = new JpaProductRepositoryAdapter(productJpa).getBySlug("house-espresso").orElseThrow();
        assertThat(seed.getPrice()).isEqualByComparingTo("3.50");
        assertThat(seed.getCategory()).isEqualTo(ProductCategory.BEANS);
    }
}

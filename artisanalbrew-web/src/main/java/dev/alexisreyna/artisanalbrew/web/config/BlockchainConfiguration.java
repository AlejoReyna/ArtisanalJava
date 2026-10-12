package dev.alexisreyna.artisanalbrew.web.config;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.config.CoffeeCoinOwnerOptions;
import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import dev.alexisreyna.artisanalbrew.application.config.DefaultChainRegistry;
import dev.alexisreyna.artisanalbrew.application.config.SolanaFaucetOptions;
import dev.alexisreyna.artisanalbrew.application.config.SponsorshipPolicyOptions;
import dev.alexisreyna.artisanalbrew.application.repository.CouponRepository;
import dev.alexisreyna.artisanalbrew.application.repository.OrderRepository;
import dev.alexisreyna.artisanalbrew.application.repository.ProductRepository;
import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerRepository;
import dev.alexisreyna.artisanalbrew.application.repository.TransparencyRecordRepository;
import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import dev.alexisreyna.artisanalbrew.application.repository.UserProfileRepository;
import dev.alexisreyna.artisanalbrew.application.service.CouponService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultCouponService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultOrderPricingService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultOrderService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultProfileService;
import dev.alexisreyna.artisanalbrew.application.service.DefaultTransparencyService;
import dev.alexisreyna.artisanalbrew.application.service.OrderPricingService;
import dev.alexisreyna.artisanalbrew.application.service.OrderService;
import dev.alexisreyna.artisanalbrew.application.service.ProfileService;
import dev.alexisreyna.artisanalbrew.application.service.TransparencyService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import dev.alexisreyna.artisanalbrew.application.service.rewards.DefaultLoyaltyMintService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.DefaultLiquidStakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.DefaultStakingLedgerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Use-case and options composition (mirrors {@code AddApplication} plus the
 * {@code Configure<T>} blocks in {@code Web/Program.cs}). The framework-free
 * {@code Default*} services are wired here so the application module keeps
 * zero Spring dependencies.
 */
@Configuration
public class BlockchainConfiguration {

    @Bean
    public ChainRegistry chainRegistry(
            @Value("${artisanalbrew.blockchain.default-chain-key:ethereum-sepolia}") String defaultChainKey,
            @Value("${artisanalbrew.blockchain.local-evm-manifest:}") String evmManifest,
            @Value("${artisanalbrew.blockchain.solana-deployment-manifest:}") String solanaManifest) {
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey(defaultChainKey);
        options = dev.alexisreyna.artisanalbrew.application.config.BlockchainManifestLoader
                .loadDeploymentManifests(options, evmManifest, solanaManifest);
        options = dev.alexisreyna.artisanalbrew.application.config.BlockchainManifestLoader
                .applyBundlerRpcUrlOverrides(options, System::getenv);
        return new DefaultChainRegistry(options);
    }

    @Bean
    public BlockchainNetworkOptions blockchainNetworkOptions(
            @Value("${artisanalbrew.network.network-name:Ethereum Sepolia}") String networkName,
            @Value("${artisanalbrew.network.chain-id:11155111}") int chainId,
            @Value("${artisanalbrew.network.chain-id-hex:0xaa36a7}") String chainIdHex,
            @Value("${artisanalbrew.network.rpc-url:https://ethereum-sepolia-rpc.publicnode.com}") String rpcUrl,
            @Value("${artisanalbrew.network.explorer-url:https://sepolia.etherscan.io/}") String explorerUrl) {
        BlockchainNetworkOptions options = new BlockchainNetworkOptions();
        options.setNetworkName(networkName);
        options.setChainId(chainId);
        options.setChainIdHex(chainIdHex);
        options.setRpcUrl(rpcUrl);
        options.setExplorerUrl(explorerUrl);
        return options;
    }

    @Bean
    public SolanaFaucetOptions solanaFaucetOptions() {
        return new SolanaFaucetOptions();
    }

    @Bean
    public SponsorshipPolicyOptions sponsorshipPolicyOptions() {
        return new SponsorshipPolicyOptions();
    }

    /**
     * Honors the exact .NET variable ({@code CoffeeCoinOwner__PrivateKey}) as
     * well as the file-based value. Empty by default: minting stays disabled
     * until an operator provides a key.
     */
    @Bean
    public CoffeeCoinOwnerOptions coffeeCoinOwnerOptions(
            @Value("${coffee-coin-owner.private-key:${CoffeeCoinOwner__PrivateKey:}}") String privateKey) {
        CoffeeCoinOwnerOptions options = new CoffeeCoinOwnerOptions();
        options.setPrivateKey(privateKey == null || privateKey.isBlank() ? null : privateKey);
        return options;
    }

    @Bean
    public OrderPricingService orderPricingService() {
        return new DefaultOrderPricingService();
    }

    @Bean
    public dev.alexisreyna.artisanalbrew.application.service.ProductService productService(
            ProductRepository products) {
        return new dev.alexisreyna.artisanalbrew.application.service.DefaultProductService(products);
    }

    @Bean
    public CouponService couponService(CouponRepository coupons, OrderPricingService pricing) {
        return new DefaultCouponService(coupons, pricing);
    }

    @Bean
    public OrderService orderService(OrderRepository orders, ProductRepository products, CouponRepository coupons,
                                     OrderPricingService pricing, MarketplacePaymentGateway payments,
                                     ChainRegistry chains, TransparencyService transparency) {
        return new DefaultOrderService(orders, products, coupons, pricing, payments, chains, transparency);
    }

    @Bean
    public ProfileService profileService(UserProfileRepository profiles) {
        return new DefaultProfileService(profiles);
    }

    @Bean
    public TransparencyService transparencyService(TransparencyRecordRepository records,
                                                   BlockchainNetworkOptions chain) {
        return new DefaultTransparencyService(records, chain);
    }

    @Bean
    public StakingLedgerService stakingLedgerService(CoffeeWeb3Service web3, StakingLedgerRepository ledger) {
        return new DefaultStakingLedgerService(web3, ledger);
    }

    @Bean
    public LiquidStakingLedgerService liquidStakingLedgerService(LiquidStakingGateway gateway,
                                                                 StakingLedgerRepository ledger) {
        return new DefaultLiquidStakingLedgerService(gateway, ledger);
    }

    @Bean
    public LoyaltyMintService loyaltyMintService(CoffeeWeb3Service web3, RewardClaimRepository claims,
                                                 UnitOfWork unitOfWork) {
        return new DefaultLoyaltyMintService(web3, claims, unitOfWork);
    }
}

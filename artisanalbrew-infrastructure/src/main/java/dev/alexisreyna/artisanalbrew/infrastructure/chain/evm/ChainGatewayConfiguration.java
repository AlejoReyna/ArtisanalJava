package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.config.CoffeeCoinOwnerOptions;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.EthUsdPriceService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit gateway wiring. The gateways stay framework-free (plain classes
 * with test-friendly constructors); only this composition root knows Spring.
 */
@Configuration
public class ChainGatewayConfiguration {

    @Bean
    public CoffeeWeb3Service coffeeWeb3Service(BlockchainNetworkOptions chain, CoffeeCoinOwnerOptions owner) {
        return new Web3jCoffeeService(chain, owner);
    }

    @Bean
    public LiquidStakingGateway liquidStakingGateway(ChainRegistry registry) {
        return new Web3jLiquidStakingGateway(registry);
    }

    @Bean
    public MarketplacePaymentGateway marketplacePaymentGateway(ChainRegistry registry) {
        return new Web3jMarketplacePaymentGateway(registry);
    }

    @Bean
    public EthUsdPriceService ethUsdPriceService() {
        return new CoinGeckoEthUsdPriceService();
    }
}

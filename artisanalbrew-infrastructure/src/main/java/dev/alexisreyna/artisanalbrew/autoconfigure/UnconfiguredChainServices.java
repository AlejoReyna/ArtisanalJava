package dev.alexisreyna.artisanalbrew.autoconfigure;

import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;
import dev.alexisreyna.artisanalbrew.infrastructure.service.LoggingSqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.infrastructure.stub.UnconfiguredCoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.infrastructure.stub.UnconfiguredLiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.infrastructure.stub.UnconfiguredRewardClaimService;
import dev.alexisreyna.artisanalbrew.infrastructure.stub.UnconfiguredSolanaFaucetService;
import dev.alexisreyna.artisanalbrew.infrastructure.service.LoggingSqsMessagePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Fail-closed defaults for services with no configured backend yet.
 * {@code @ConditionalOnMissingBean} lives here — on an explicit configuration —
 * because on component-scanned classes it is order-dependent and silently
 * drops beans. Real implementations back these off automatically.
 */
@AutoConfiguration
public class UnconfiguredChainServices {

    @Bean
    @ConditionalOnMissingBean(CoffeeWeb3Service.class)
    public CoffeeWeb3Service unconfiguredCoffeeWeb3Service() {
        return new UnconfiguredCoffeeWeb3Service();
    }

    @Bean
    @ConditionalOnMissingBean(LiquidStakingGateway.class)
    public LiquidStakingGateway unconfiguredLiquidStakingGateway() {
        return new UnconfiguredLiquidStakingGateway();
    }

    @Bean
    @ConditionalOnMissingBean(SolanaFaucetService.class)
    public SolanaFaucetService unconfiguredSolanaFaucetService() {
        return new UnconfiguredSolanaFaucetService();
    }

    @Bean
    @ConditionalOnMissingBean(RewardClaimService.class)
    public RewardClaimService unconfiguredRewardClaimService() {
        return new UnconfiguredRewardClaimService();
    }

    @Bean
    @ConditionalOnMissingBean(SqsMessagePublisher.class)
    public SqsMessagePublisher loggingSqsMessagePublisher() {
        return new LoggingSqsMessagePublisher();
    }
}

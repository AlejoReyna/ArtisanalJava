package dev.alexisreyna.artisanalbrew.infrastructure.stub;

import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;
import dev.alexisreyna.artisanalbrew.infrastructure.service.LoggingSqsMessagePublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fail-closed defaults for services with no configured backend yet.
 * {@code @ConditionalOnMissingBean} lives here — on an explicit configuration —
 * because on component-scanned classes it is order-dependent and silently
 * drops beans. Real implementations back these off automatically.
 */
@Configuration
public class UnconfiguredChainServices {

    @Bean
    public CoffeeWeb3Service coffeeWeb3Service() {
        return new UnconfiguredCoffeeWeb3Service();
    }

    @Bean
    public LiquidStakingGateway liquidStakingGateway() {
        return new UnconfiguredLiquidStakingGateway();
    }

    @Bean
    public SolanaFaucetService solanaFaucetService() {
        return new UnconfiguredSolanaFaucetService();
    }

    @Bean
    public RewardClaimService rewardClaimService() {
        return new UnconfiguredRewardClaimService();
    }

    @Bean
    @ConditionalOnMissingBean(SqsMessagePublisher.class)
    public SqsMessagePublisher sqsMessagePublisher() {
        return new LoggingSqsMessagePublisher();
    }
}

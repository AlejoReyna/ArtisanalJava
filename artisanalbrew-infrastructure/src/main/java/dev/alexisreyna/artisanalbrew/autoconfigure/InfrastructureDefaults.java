package dev.alexisreyna.artisanalbrew.autoconfigure;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.config.CoffeeCoinOwnerOptions;
import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import dev.alexisreyna.artisanalbrew.application.config.DefaultChainRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.time.Clock;

/**
 * Fail-closed defaults (mirrors {@code Program.cs} registrations like
 * {@code TimeProvider.System}): the web composition overrides these with
 * configured values; tests and unconfigured runs get safe empties.
 */
@AutoConfiguration
public class InfrastructureDefaults {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean(CrossChainSolverOptions.class)
    public CrossChainSolverOptions crossChainSolverOptions() {
        return new CrossChainSolverOptions();
    }

    @Bean
    @ConditionalOnMissingBean(ChainRegistry.class)
    public ChainRegistry chainRegistry() {
        return new DefaultChainRegistry(BlockchainOptions.createDefaults());
    }

    @Bean
    @ConditionalOnMissingBean(BlockchainNetworkOptions.class)
    public BlockchainNetworkOptions blockchainNetworkOptions() {
        return new BlockchainNetworkOptions();
    }

    @Bean
    @ConditionalOnMissingBean(CoffeeCoinOwnerOptions.class)
    public CoffeeCoinOwnerOptions coffeeCoinOwnerOptions() {
        return new CoffeeCoinOwnerOptions();
    }
}

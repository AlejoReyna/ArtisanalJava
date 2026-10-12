package dev.alexisreyna.artisanalbrew.infrastructure.config;

import dev.alexisreyna.artisanalbrew.application.config.CrossChainSolverOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Fail-closed defaults (mirrors {@code Program.cs} registrations like
 * {@code TimeProvider.System}): the web composition overrides these with
 * configured values; tests and unconfigured runs get safe empties.
 */
@Configuration
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
}

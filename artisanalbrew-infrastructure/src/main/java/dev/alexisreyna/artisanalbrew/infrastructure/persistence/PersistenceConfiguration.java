package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Composition root for persistence (mirrors {@code AddInfrastructure}).
 * Web/Worker import this; the application layer never sees it.
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo")
public class PersistenceConfiguration {
}

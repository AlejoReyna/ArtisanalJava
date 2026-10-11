package dev.alexisreyna.artisanalbrew.application.service.commerce;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.repository.AgenticCommerceProjectionBatch;

/** Port of {@code IAgenticCommerceReconciliationApplicator}. */
public interface AgenticCommerceReconciliationApplicator {
    void applyEvent(AgenticCommerceProjectionBatch batch, ChainDefinition chain, String escrowAddress, EscrowEvent event);

    void applyRegistryEvent(AgenticCommerceProjectionBatch batch, ChainDefinition chain, String registryAddress, RegistryEvent event);
}

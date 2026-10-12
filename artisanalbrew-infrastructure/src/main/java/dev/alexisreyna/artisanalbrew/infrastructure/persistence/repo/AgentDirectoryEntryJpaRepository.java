package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgentDirectoryEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentDirectoryEntryJpaRepository extends JpaRepository<AgentDirectoryEntryEntity, UUID> {
    boolean existsByChainKeyAndRegistryAddressAndAgentId(String chainKey, String registryAddress, long agentId);
}

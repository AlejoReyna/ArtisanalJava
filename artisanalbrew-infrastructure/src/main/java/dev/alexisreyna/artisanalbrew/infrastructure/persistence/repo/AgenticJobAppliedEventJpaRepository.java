package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobAppliedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgenticJobAppliedEventJpaRepository extends JpaRepository<AgenticJobAppliedEventEntity, UUID> {
    boolean existsByChainKeyAndContractAddressAndTransactionHashAndLogIndex(
            String chainKey, String contractAddress, String transactionHash, int logIndex);
}

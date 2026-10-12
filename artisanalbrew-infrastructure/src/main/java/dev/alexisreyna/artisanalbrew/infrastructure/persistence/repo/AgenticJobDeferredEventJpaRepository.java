package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobDeferredEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgenticJobDeferredEventJpaRepository extends JpaRepository<AgenticJobDeferredEventEntity, UUID> {
    boolean existsByChainKeyAndContractAddressAndTransactionHashAndLogIndex(
            String chainKey, String contractAddress, String transactionHash, int logIndex);
}

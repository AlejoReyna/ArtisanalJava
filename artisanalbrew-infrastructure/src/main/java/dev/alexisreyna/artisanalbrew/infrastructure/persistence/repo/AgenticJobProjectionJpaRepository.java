package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgenticJobProjectionJpaRepository extends JpaRepository<AgenticJobProjectionEntity, UUID> {
    Optional<AgenticJobProjectionEntity> findByChainKeyAndContractAddressAndOnChainJobId(
            String chainKey, String contractAddress, long onChainJobId);
}

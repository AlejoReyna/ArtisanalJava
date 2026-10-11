package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.domain.entities.AgentDirectoryEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Port of {@code Services/IAgenticJobService.cs}. */
public interface AgenticJobService {
    List<AgentDirectoryEntry> getAgents(String chainKey);

    List<AgenticJobProjection> getJobs(String chainKey, String userAddress);

    Optional<AgenticJobProjection> getJob(UUID id);

    AgenticJobProjection createJob(String chainKey, String clientAddress, String providerAddress,
                                   String evaluatorAddress, String descriptionCommitment,
                                   BigDecimal budget, long expiredAt);

    void advanceJobStatus(UUID id, String expectedStatus, String newStatus, Consumer<AgenticJobProjection> update);

    default void advanceJobStatus(UUID id, String expectedStatus, String newStatus) {
        advanceJobStatus(id, expectedStatus, newStatus, null);
    }

    /**
     * Advances a job from a verified on-chain event. Idempotent: re-processing an
     * event for a job already in the target state is a no-op.
     */
    void advanceJobWithTransaction(UUID id, String expectedStatus, String newStatus,
                                   String transactionHash, Consumer<AgenticJobProjection> update);

    default void advanceJobWithTransaction(UUID id, String expectedStatus, String newStatus, String transactionHash) {
        advanceJobWithTransaction(id, expectedStatus, newStatus, transactionHash, null);
    }
}

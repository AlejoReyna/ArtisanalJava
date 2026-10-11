package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobAppliedEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobDeferredEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.domain.entities.AgentDirectoryEntry;

import java.util.Optional;

/**
 * Port of {@code Repositories/IAgenticCommerceProjectionBatch.cs}.
 * Stages writes on the caller's unit of work without persisting; the worker
 * commits projections and checkpoint atomically.
 */
public interface AgenticCommerceProjectionBatch {
    boolean hasAppliedEvent(String chainKey, String contractAddress, String transactionHash, int logIndex);

    boolean hasDeferredEvent(String chainKey, String contractAddress, String transactionHash, int logIndex);

    Optional<AgenticJobProjection> findJob(String chainKey, String contractAddress, long onChainJobId);

    boolean hasAgentDirectoryEntry(String chainKey, String registryAddress, long agentId);

    void stageJob(AgenticJobProjection job);

    void stageAgentDirectoryEntry(AgentDirectoryEntry entry);

    void stageAppliedEvent(AgenticJobAppliedEvent appliedEvent);

    void stageDeferredEvent(AgenticJobDeferredEvent deferredEvent);
}

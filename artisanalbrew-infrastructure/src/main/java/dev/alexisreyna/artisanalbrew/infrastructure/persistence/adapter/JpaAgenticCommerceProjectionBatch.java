package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.AgenticCommerceProjectionBatch;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobAppliedEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobDeferredEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.domain.entities.AgentDirectoryEntry;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobAppliedEventEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobDeferredEventEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgenticJobProjectionEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.AgentDirectoryEntryEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobAppliedEventJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobDeferredEventJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobProjectionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgentDirectoryEntryJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * JPA staging batch for {@link AgenticCommerceProjectionBatch}. Stages attach
 * to the caller's transaction and are never flushed here — the worker commits
 * projections and checkpoint atomically (mirrors {@code AgenticCommerceProjectionBatch.cs}).
 */
@Repository
@Transactional
public class JpaAgenticCommerceProjectionBatch implements AgenticCommerceProjectionBatch {
    private final AgenticJobProjectionJpaRepository jobs;
    private final AgentDirectoryEntryJpaRepository directory;
    private final AgenticJobAppliedEventJpaRepository applied;
    private final AgenticJobDeferredEventJpaRepository deferred;

    public JpaAgenticCommerceProjectionBatch(AgenticJobProjectionJpaRepository jobs,
                                             AgentDirectoryEntryJpaRepository directory,
                                             AgenticJobAppliedEventJpaRepository applied,
                                             AgenticJobDeferredEventJpaRepository deferred) {
        this.jobs = jobs;
        this.directory = directory;
        this.applied = applied;
        this.deferred = deferred;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAppliedEvent(String chainKey, String contractAddress, String transactionHash, int logIndex) {
        return applied.existsByChainKeyAndContractAddressAndTransactionHashAndLogIndex(
                chainKey, contractAddress, transactionHash, logIndex);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasDeferredEvent(String chainKey, String contractAddress, String transactionHash, int logIndex) {
        return deferred.existsByChainKeyAndContractAddressAndTransactionHashAndLogIndex(
                chainKey, contractAddress, transactionHash, logIndex);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgenticJobProjection> findJob(String chainKey, String contractAddress, long onChainJobId) {
        return jobs.findByChainKeyAndContractAddressAndOnChainJobId(chainKey, contractAddress, onChainJobId)
                .map(JpaAgenticCommerceProjectionBatch::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAgentDirectoryEntry(String chainKey, String registryAddress, long agentId) {
        return directory.existsByChainKeyAndRegistryAddressAndAgentId(chainKey, registryAddress, agentId);
    }

    @Override
    public void stageJob(AgenticJobProjection job) {
        Optional<AgenticJobProjectionEntity> existing = jobs
                .findByChainKeyAndContractAddressAndOnChainJobId(
                        job.getChainKey(), job.getContractAddress(), job.getOnChainJobId());
        if (existing.isPresent()) {
            copyJob(job, existing.get());
            jobs.save(existing.get());
        } else {
            AgenticJobProjectionEntity e = new AgenticJobProjectionEntity();
            e.id = job.getId();
            copyJob(job, e);
            jobs.save(e);
        }
    }

    @Override
    public void stageAgentDirectoryEntry(AgentDirectoryEntry entry) {
        AgentDirectoryEntryEntity e = new AgentDirectoryEntryEntity();
        e.id = entry.getId();
        e.chainKey = entry.getChainKey();
        e.registryAddress = entry.getRegistryAddress();
        e.agentId = entry.getAgentId();
        e.ownerAddress = entry.getOwnerAddress();
        e.metadataUri = entry.getMetadataUri();
        e.active = entry.isActive();
        e.createdAtUtc = entry.getCreatedAtUtc();
        e.updatedAtUtc = entry.getUpdatedAtUtc();
        directory.save(e);
    }

    @Override
    public void stageAppliedEvent(AgenticJobAppliedEvent event) {
        AgenticJobAppliedEventEntity e = new AgenticJobAppliedEventEntity();
        e.id = event.getId();
        e.chainKey = event.getChainKey();
        e.contractAddress = event.getContractAddress();
        e.transactionHash = event.getTransactionHash();
        e.logIndex = event.getLogIndex();
        e.appliedAtUtc = event.getAppliedAtUtc();
        applied.save(e);
    }

    @Override
    public void stageDeferredEvent(AgenticJobDeferredEvent event) {
        AgenticJobDeferredEventEntity e = new AgenticJobDeferredEventEntity();
        e.id = event.getId();
        e.chainKey = event.getChainKey();
        e.contractAddress = event.getContractAddress();
        e.onChainJobId = event.getOnChainJobId();
        e.eventType = event.getEventType();
        e.transactionHash = event.getTransactionHash();
        e.logIndex = event.getLogIndex();
        e.blockNumber = event.getBlockNumber();
        e.deferralReason = event.getDeferralReason();
        e.deferredAtUtc = event.getDeferredAtUtc();
        deferred.save(e);
    }

    static void copyJob(AgenticJobProjection job, AgenticJobProjectionEntity e) {
        e.chainKey = job.getChainKey();
        e.escrowAddress = job.getEscrowAddress();
        e.jobId = job.getJobId();
        e.clientAddress = job.getClientAddress();
        e.providerAddress = job.getProviderAddress();
        e.evaluatorAddress = job.getEvaluatorAddress();
        e.descriptionCommitment = job.getDescriptionCommitment();
        e.budget = job.getBudget();
        e.expiredAt = job.getExpiredAt();
        e.status = job.getStatus();
        e.deliverableCommitment = job.getDeliverableCommitment();
        e.decisionReason = job.getDecisionReason();
        e.createdAtUtc = job.getCreatedAtUtc();
        e.updatedAtUtc = job.getUpdatedAtUtc();
        e.onChainJobId = job.getOnChainJobId();
        e.chainId = job.getChainId();
        e.contractAddress = job.getContractAddress();
        e.creationTransactionHash = job.getCreationTransactionHash();
        e.fundedTransactionHash = job.getFundedTransactionHash();
        e.completionTransactionHash = job.getCompletionTransactionHash();
        e.lastReconciledBlock = job.getLastReconciledBlock();
        e.concurrencyToken = job.getConcurrencyToken();
    }

    static AgenticJobProjection toDomain(AgenticJobProjectionEntity e) {
        AgenticJobProjection job = new AgenticJobProjection();
        job.setId(e.id);
        job.setChainKey(e.chainKey);
        job.setEscrowAddress(e.escrowAddress);
        job.setJobId(e.jobId);
        job.setClientAddress(e.clientAddress);
        job.setProviderAddress(e.providerAddress);
        job.setEvaluatorAddress(e.evaluatorAddress);
        job.setDescriptionCommitment(e.descriptionCommitment);
        job.setBudget(e.budget);
        job.setExpiredAt(e.expiredAt);
        job.setStatus(e.status);
        job.setDeliverableCommitment(e.deliverableCommitment);
        job.setDecisionReason(e.decisionReason);
        job.setCreatedAtUtc(e.createdAtUtc);
        job.setUpdatedAtUtc(e.updatedAtUtc);
        job.setOnChainJobId(e.onChainJobId);
        job.setChainId(e.chainId);
        job.setContractAddress(e.contractAddress);
        job.setCreationTransactionHash(e.creationTransactionHash);
        job.setFundedTransactionHash(e.fundedTransactionHash);
        job.setCompletionTransactionHash(e.completionTransactionHash);
        job.setLastReconciledBlock(e.lastReconciledBlock);
        job.setConcurrencyToken(e.concurrencyToken);
        return job;
    }
}

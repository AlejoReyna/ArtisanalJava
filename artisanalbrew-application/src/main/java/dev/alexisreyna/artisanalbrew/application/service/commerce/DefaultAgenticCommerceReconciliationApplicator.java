package dev.alexisreyna.artisanalbrew.application.service.commerce;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.repository.AgenticCommerceProjectionBatch;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobAppliedEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobDeferredEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.domain.entities.AgentDirectoryEntry;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Port of {@code AgenticCommerceReconciliationApplicator} in
 * {@code AgenticCommerce/AgenticCommerceReconciliationApplicator.cs}.
 *
 * <p>Consistency policy: duplicates idempotent-skip; missing prerequisites and
 * wrong-order events durably deferred (checkpoint must not advance past them);
 * terminal-event duplicates silently skipped. Never persists — the caller commits
 * projections and checkpoint atomically.</p>
 */
public final class DefaultAgenticCommerceReconciliationApplicator implements AgenticCommerceReconciliationApplicator {
    private static final BigDecimal WEI_PER_UNIT = BigDecimal.TEN.pow(18);

    @Override
    public void applyEvent(AgenticCommerceProjectionBatch batch, ChainDefinition chain,
                           String escrowAddress, EscrowEvent event) {
        Integer chainId = chain.getEvmChainId();
        long chainIdValue = chainId == null ? 0 : chainId;

        if (batch.hasAppliedEvent(chain.getKey(), escrowAddress, event.getTransactionHash(), event.getLogIndex())) {
            return;
        }
        boolean alreadyDeferred = batch.hasDeferredEvent(
                chain.getKey(), escrowAddress, event.getTransactionHash(), event.getLogIndex());

        // The job this event mutates, if any. Unlike EF Core change tracking —
        // where FindJobAsync returns a tracked entity saved by the caller's
        // SaveChanges — staged mutations here must be written back explicitly.
        AgenticJobProjection touched = null;

        switch (event.getType()) {
            case JOB_CREATED -> {
                if (batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).isEmpty()) {
                    AgenticJobProjection job = new AgenticJobProjection();
                    job.setChainKey(chain.getKey());
                    job.setOnChainJobId(event.getOnChainJobId());
                    job.setChainId(chainIdValue);
                    job.setContractAddress(escrowAddress);
                    job.setEscrowAddress(escrowAddress);
                    job.setClientAddress(event.getClient());
                    job.setProviderAddress(event.getProvider());
                    job.setEvaluatorAddress(event.getEvaluator());
                    job.setExpiredAt(event.getExpiredAt().longValue());
                    job.setStatus(AgenticJobProjection.STATUS_OPEN);
                    job.setCreationTransactionHash(event.getTransactionHash());
                    job.setLastReconciledBlock(event.getBlockNumber());
                    job.setCreatedAtUtc(Instant.now());
                    job.setUpdatedAtUtc(Instant.now());
                    batch.stageJob(job);
                }
                // Idempotent either way: the existing projection already covers this identity.
            }
            case PROVIDER_SET -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for ProviderSet");
                    }
                    return;
                }
                if (!AgenticJobProjection.STATUS_OPEN.equals(job.getStatus())) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Invalid state " + job.getStatus() + " for ProviderSet");
                    }
                    return;
                }
                job.setProviderAddress(event.getProvider());
                touched = touch(job, event);
            }
            case BUDGET_SET -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for BudgetSet");
                    }
                    return;
                }
                if (!AgenticJobProjection.STATUS_OPEN.equals(job.getStatus())) {
                    // Budget already set via fund — silently ignored.
                    break;
                }
                job.setBudget(toUnits(event.getAmount()));
                touched = touch(job, event);
            }
            case JOB_FUNDED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for JobFunded");
                    }
                    return;
                }
                if (!AgenticJobProjection.STATUS_OPEN.equals(job.getStatus())) {
                    // Duplicate fund on funded/terminal job: idempotent skip.
                    break;
                }
                job.setStatus(AgenticJobProjection.STATUS_FUNDED);
                job.setBudget(toUnits(event.getAmount()));
                job.setFundedTransactionHash(event.getTransactionHash());
                touched = touch(job, event);
            }
            case JOB_SUBMITTED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for JobSubmitted");
                    }
                    return;
                }
                if (!AgenticJobProjection.STATUS_FUNDED.equals(job.getStatus())) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Invalid state " + job.getStatus() + " for JobSubmitted (expected Funded)");
                    }
                    return;
                }
                job.setStatus(AgenticJobProjection.STATUS_SUBMITTED);
                job.setDeliverableCommitment(event.getDeliverable());
                touched = touch(job, event);
            }
            case JOB_COMPLETED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for JobCompleted");
                    }
                    return;
                }
                if (AgenticJobProjection.STATUS_COMPLETED.equals(job.getStatus())) {
                    break; // Idempotent duplicate terminal event.
                }
                if (!AgenticJobProjection.STATUS_SUBMITTED.equals(job.getStatus())) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Invalid state " + job.getStatus() + " for JobCompleted (expected Submitted)");
                    }
                    return;
                }
                job.setStatus(AgenticJobProjection.STATUS_COMPLETED);
                job.setDecisionReason(event.getReason());
                job.setCompletionTransactionHash(event.getTransactionHash());
                touched = touch(job, event);
            }
            case JOB_REJECTED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for JobRejected");
                    }
                    return;
                }
                if (AgenticJobProjection.STATUS_COMPLETED.equals(job.getStatus())
                        || AgenticJobProjection.STATUS_REJECTED.equals(job.getStatus())
                        || AgenticJobProjection.STATUS_EXPIRED.equals(job.getStatus())) {
                    break; // Idempotent duplicate terminal event.
                }
                if (!AgenticJobProjection.STATUS_SUBMITTED.equals(job.getStatus())) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Invalid state " + job.getStatus() + " for JobRejected (expected Submitted)");
                    }
                    return;
                }
                job.setStatus(AgenticJobProjection.STATUS_REJECTED);
                job.setDecisionReason(event.getReason());
                job.setCompletionTransactionHash(event.getTransactionHash());
                touched = touch(job, event);
            }
            case JOB_EXPIRED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event, "Job not found for JobExpired");
                    }
                    return;
                }
                if (AgenticJobProjection.STATUS_EXPIRED.equals(job.getStatus())) {
                    break; // Idempotent duplicate terminal event.
                }
                if (!AgenticJobProjection.STATUS_FUNDED.equals(job.getStatus())) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Invalid state " + job.getStatus() + " for JobExpired (expected Funded)");
                    }
                    return;
                }
                job.setStatus(AgenticJobProjection.STATUS_EXPIRED);
                job.setCompletionTransactionHash(event.getTransactionHash());
                touched = touch(job, event);
            }
            case PAYMENT_RELEASED, REFUNDED -> {
                var job = batch.findJob(chain.getKey(), escrowAddress, event.getOnChainJobId()).orElse(null);
                if (job == null) {
                    if (!alreadyDeferred) {
                        recordDeferred(batch, chain.getKey(), escrowAddress, event,
                                "Job not found for " + event.getType());
                    }
                    return;
                }
                touched = touch(job, event);
            }
        }

        // Reached only by applied/idempotent paths — never by deferred early-returns.
        // Explicit write-back: without EF change tracking, the mutated projection
        // above would otherwise never reach the database.
        if (touched != null) {
            batch.stageJob(touched);
        }
        AgenticJobAppliedEvent applied = new AgenticJobAppliedEvent();
        applied.setChainKey(chain.getKey());
        applied.setContractAddress(escrowAddress);
        applied.setTransactionHash(event.getTransactionHash());
        applied.setLogIndex(event.getLogIndex());
        applied.setAppliedAtUtc(Instant.now());
        batch.stageAppliedEvent(applied);
    }

    @Override
    public void applyRegistryEvent(AgenticCommerceProjectionBatch batch, ChainDefinition chain,
                                   String registryAddress, RegistryEvent event) {
        if (batch.hasAppliedEvent(chain.getKey(), registryAddress, event.getTransactionHash(), event.getLogIndex())) {
            return;
        }
        if (!batch.hasAgentDirectoryEntry(chain.getKey(), registryAddress, event.getAgentId())) {
            AgentDirectoryEntry entry = new AgentDirectoryEntry();
            entry.setChainKey(chain.getKey());
            entry.setAgentId(event.getAgentId());
            entry.setRegistryAddress(registryAddress);
            entry.setOwnerAddress(event.getOwner());
            entry.setMetadataUri(event.getMetadataUri());
            entry.setActive(true);
            entry.setCreatedAtUtc(Instant.now());
            entry.setUpdatedAtUtc(Instant.now());
            batch.stageAgentDirectoryEntry(entry);
        }
        AgenticJobAppliedEvent applied = new AgenticJobAppliedEvent();
        applied.setChainKey(chain.getKey());
        applied.setContractAddress(registryAddress);
        applied.setTransactionHash(event.getTransactionHash());
        applied.setLogIndex(event.getLogIndex());
        applied.setAppliedAtUtc(Instant.now());
        batch.stageAppliedEvent(applied);
    }

    private static AgenticJobProjection touch(AgenticJobProjection job, EscrowEvent event) {
        job.setLastReconciledBlock(event.getBlockNumber());
        job.setUpdatedAtUtc(Instant.now());
        job.setConcurrencyToken(job.getConcurrencyToken() + 1);
        return job;
    }

    private static void recordDeferred(AgenticCommerceProjectionBatch batch, String chainKey,
                                       String escrowAddress, EscrowEvent event, String reason) {
        AgenticJobDeferredEvent deferred = new AgenticJobDeferredEvent();
        deferred.setChainKey(chainKey);
        deferred.setContractAddress(escrowAddress);
        deferred.setOnChainJobId(event.getOnChainJobId());
        deferred.setEventType(toEventTypeName(event.getType()));
        deferred.setTransactionHash(event.getTransactionHash());
        deferred.setLogIndex(event.getLogIndex());
        deferred.setBlockNumber(event.getBlockNumber());
        deferred.setDeferralReason(reason);
        deferred.setDeferredAtUtc(Instant.now());
        batch.stageDeferredEvent(deferred);
    }

    private static BigDecimal toUnits(java.math.BigInteger wei) {
        return new BigDecimal(wei).divide(WEI_PER_UNIT);
    }

    /** .NET {@code EscrowEventType.ToString()} names (e.g. "JobCreated"), preserved in deferred rows. */
    static String toEventTypeName(EscrowEventType type) {
        return switch (type) {
            case JOB_CREATED -> "JobCreated";
            case PROVIDER_SET -> "ProviderSet";
            case BUDGET_SET -> "BudgetSet";
            case JOB_FUNDED -> "JobFunded";
            case JOB_SUBMITTED -> "JobSubmitted";
            case JOB_COMPLETED -> "JobCompleted";
            case JOB_REJECTED -> "JobRejected";
            case JOB_EXPIRED -> "JobExpired";
            case PAYMENT_RELEASED -> "PaymentReleased";
            case REFUNDED -> "Refunded";
        };
    }
}

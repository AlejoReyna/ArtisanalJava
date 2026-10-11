package dev.alexisreyna.artisanalbrew.application.service.commerce;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.repository.AgenticCommerceProjectionBatch;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobAppliedEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobDeferredEvent;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.domain.entities.AgentDirectoryEntry;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicatorTest {

    static final class FakeBatch implements AgenticCommerceProjectionBatch {
        final Set<String> applied = new HashSet<>();
        final Set<String> deferredKeys = new HashSet<>();
        final Map<String, AgenticJobProjection> jobs = new HashMap<>();
        final Set<String> directory = new HashSet<>();
        final java.util.List<AgenticJobDeferredEvent> deferredRows = new java.util.ArrayList<>();

        static String logKey(String chain, String contract, String tx, int index) {
            return chain + "|" + contract + "|" + tx + "|" + index;
        }

        static String jobKey(String chain, String contract, long jobId) {
            return chain + "|" + contract + "|" + jobId;
        }

        @Override
        public boolean hasAppliedEvent(String chainKey, String contractAddress, String transactionHash, int logIndex) {
            return applied.contains(logKey(chainKey, contractAddress, transactionHash, logIndex));
        }

        @Override
        public boolean hasDeferredEvent(String chainKey, String contractAddress, String transactionHash, int logIndex) {
            return deferredKeys.contains(logKey(chainKey, contractAddress, transactionHash, logIndex));
        }

        @Override
        public Optional<AgenticJobProjection> findJob(String chainKey, String contractAddress, long onChainJobId) {
            return Optional.ofNullable(jobs.get(jobKey(chainKey, contractAddress, onChainJobId)));
        }

        @Override
        public boolean hasAgentDirectoryEntry(String chainKey, String registryAddress, long agentId) {
            return directory.contains(jobKey(chainKey, registryAddress, agentId));
        }

        @Override
        public void stageJob(AgenticJobProjection job) {
            jobs.put(jobKey(job.getChainKey(), job.getContractAddress(), job.getOnChainJobId()), job);
        }

        @Override
        public void stageAgentDirectoryEntry(AgentDirectoryEntry entry) {
            directory.add(jobKey(entry.getChainKey(), entry.getRegistryAddress(), entry.getAgentId()));
        }

        @Override
        public void stageAppliedEvent(AgenticJobAppliedEvent event) {
            applied.add(logKey(event.getChainKey(), event.getContractAddress(),
                    event.getTransactionHash(), event.getLogIndex()));
        }

        @Override
        public void stageDeferredEvent(AgenticJobDeferredEvent event) {
            deferredKeys.add(logKey(event.getChainKey(), event.getContractAddress(),
                    event.getTransactionHash(), event.getLogIndex()));
            deferredRows.add(event);
        }
    }

    private static ChainDefinition chain() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEvmChainId(11155111);
        return chain;
    }

    private static EscrowEvent event(EscrowEventType type, long jobId, String tx, int index) {
        EscrowEvent event = new EscrowEvent();
        event.setType(type);
        event.setOnChainJobId(jobId);
        event.setClient("0xclient");
        event.setProvider("0xprovider");
        event.setEvaluator("0xevaluator");
        event.setAmount(BigInteger.TEN.pow(18));
        event.setDeliverable("QmDeliverable");
        event.setReason("looks good");
        event.setTransactionHash(tx);
        event.setBlockNumber(100L);
        event.setLogIndex(index);
        return event;
    }

    @Test
    void fullLifecycleIsIdempotent() {
        FakeBatch batch = new FakeBatch();
        var applicator = new DefaultAgenticCommerceReconciliationApplicator();
        ChainDefinition chain = chain();
        String escrow = "0xescrow";

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_CREATED, 7L, "0xtx1", 0));
        AgenticJobProjection job = batch.findJob("ethereum-sepolia", escrow, 7L).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(AgenticJobProjection.STATUS_OPEN);
        // Same log identity re-processed: idempotent skip.
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_CREATED, 7L, "0xtx1", 0));
        assertThat(batch.deferredRows).isEmpty();

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_FUNDED, 7L, "0xtx2", 0));
        assertThat(job.getStatus()).isEqualTo(AgenticJobProjection.STATUS_FUNDED);
        assertThat(job.getBudget()).isEqualByComparingTo("1");
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_SUBMITTED, 7L, "0xtx3", 0));
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_COMPLETED, 7L, "0xtx4", 0));
        assertThat(job.getStatus()).isEqualTo(AgenticJobProjection.STATUS_COMPLETED);
        assertThat(job.getDecisionReason()).isEqualTo("looks good");
        // Duplicate terminal event: silently skipped.
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_COMPLETED, 7L, "0xtx4", 0));
        assertThat(batch.deferredRows).isEmpty();
        assertThat(job.getConcurrencyToken()).isPositive();
    }

    @Test
    void missingPrerequisiteAndWrongOrderDefer() {
        FakeBatch batch = new FakeBatch();
        var applicator = new DefaultAgenticCommerceReconciliationApplicator();
        ChainDefinition chain = chain();
        String escrow = "0xescrow";

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_FUNDED, 9L, "0xtx9", 0));
        assertThat(batch.deferredRows).hasSize(1);
        assertThat(batch.deferredRows.get(0).getDeferralReason()).contains("Job not found");

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_CREATED, 9L, "0xtx8", 0));
        // Completing an Open job skips Funded/Submitted: deferred, .NET-style event name kept.
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_COMPLETED, 9L, "0xtx10", 0));
        assertThat(batch.deferredRows).hasSize(2);
        assertThat(batch.deferredRows.get(1).getEventType()).isEqualTo("JobCompleted");
    }

    @Test
    void registryEventsProjectDirectoryEntries() {
        FakeBatch batch = new FakeBatch();
        var applicator = new DefaultAgenticCommerceReconciliationApplicator();
        RegistryEvent event = new RegistryEvent();
        event.setAgentId(42L);
        event.setOwner("0xowner");
        event.setMetadataUri("ipfs://meta");
        event.setTransactionHash("0xreg");
        event.setBlockNumber(50L);
        event.setLogIndex(3);
        applicator.applyRegistryEvent(batch, chain(), "0xregistry", event);
        assertThat(batch.hasAgentDirectoryEntry("ethereum-sepolia", "0xregistry", 42L)).isTrue();
        applicator.applyRegistryEvent(batch, chain(), "0xregistry", event);
        assertThat(batch.directory).hasSize(1);
    }
}

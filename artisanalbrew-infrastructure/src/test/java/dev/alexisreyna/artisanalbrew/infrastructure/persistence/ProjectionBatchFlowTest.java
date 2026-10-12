package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.service.commerce.DefaultAgenticCommerceReconciliationApplicator;
import dev.alexisreyna.artisanalbrew.application.service.commerce.EscrowEvent;
import dev.alexisreyna.artisanalbrew.application.service.commerce.EscrowEventType;
import dev.alexisreyna.artisanalbrew.domain.entities.AgenticJobProjection;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaAgenticCommerceProjectionBatch;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobAppliedEventJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobDeferredEventJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgenticJobProjectionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.AgentDirectoryEntryJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reconciliation applicator over the JPA staging batch: full lifecycle
 * persists, and a premature event durably defers instead of advancing.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class ProjectionBatchFlowTest {

    @Autowired
    private AgenticJobProjectionJpaRepository jobs;
    @Autowired
    private AgentDirectoryEntryJpaRepository directory;
    @Autowired
    private AgenticJobAppliedEventJpaRepository applied;
    @Autowired
    private AgenticJobDeferredEventJpaRepository deferred;

    private static EscrowEvent event(EscrowEventType type, long jobId, String tx, int index) {
        EscrowEvent event = new EscrowEvent();
        event.setType(type);
        event.setOnChainJobId(jobId);
        event.setClient("0xclient");
        event.setProvider("0xprovider");
        event.setEvaluator("0xevaluator");
        event.setAmount(BigInteger.TEN.pow(18));
        event.setTransactionHash(tx);
        event.setBlockNumber(10L);
        event.setLogIndex(index);
        return event;
    }

    @Test
    void lifecyclePersistsAndPrematureEventDefers() {
        var batch = new JpaAgenticCommerceProjectionBatch(jobs, directory, applied, deferred);
        var applicator = new DefaultAgenticCommerceReconciliationApplicator();
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEvmChainId(11155111);
        String escrow = "0xescrow";

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_CREATED, 1L, "0xa", 0));
        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_FUNDED, 1L, "0xb", 0));
        AgenticJobProjection job =
                batch.findJob("ethereum-sepolia", escrow, 1L).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(AgenticJobProjection.STATUS_FUNDED);
        assertThat(job.getBudget()).isEqualByComparingTo("1");

        // Fresh persistence context read-back: proves rows actually committed
        // through the EntityManager, not just the first-level cache.
        jobs.flush();
        assertThat(jobs.findByChainKeyAndContractAddressAndOnChainJobId(
                "ethereum-sepolia", escrow, 1L)).isPresent();

        applicator.applyEvent(batch, chain, escrow, event(EscrowEventType.JOB_COMPLETED, 2L, "0xc", 0));
        assertThat(deferred.count()).isEqualTo(1);
        assertThat(batch.findJob("ethereum-sepolia", escrow, 2L)).isEmpty();
    }
}

package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerWriteResult;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaRewardClaimRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaStakingLedgerRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaUnitOfWork;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Idempotent-write semantics against the real constraints: unique ledger
 * operation key, filtered unique payment hash, and UnitOfWork commit/rollback.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class IdempotentWriteTest {

    @Autowired
    private JpaStakingLedgerRepositoryAdapter ledger;
    @Autowired
    private JpaRewardClaimRepositoryAdapter claims;
    @Autowired
    private JpaUnitOfWork unitOfWork;

    private static StakingLedgerEntry entry(String hash, int index) {
        StakingLedgerEntry entry = StakingLedgerEntry.create("ethereum-sepolia", hash, index, e -> {
            e.setWalletAddress("0xwallet");
            e.setActionType("deposit");
            e.setAmount(BigDecimal.ONE);
            e.setChainId(11155111);
            e.setNetworkName("Ethereum Sepolia");
            e.setRecordedAtUtc(Instant.now());
        });
        return entry;
    }

    @Test
    void ledgerWriteIsIdempotent() {
        StakingLedgerWriteResult first = ledger.addIfAbsent(entry("0xaaa", 0));
        assertThat(first.added()).isTrue();
        StakingLedgerWriteResult second = ledger.addIfAbsent(entry("0xaaa", 0));
        assertThat(second.added()).isFalse();
        assertThat(second.existing()).isNotNull();
        assertThat(ledger.findByOperation("ethereum-sepolia", "0xaaa", 0)).isPresent();
        assertThat(ledger.findByOperation("ethereum-sepolia", "0xaaa", 1)).isEmpty();
    }

    @Test
    void rewardClaimTryAddHonorsUniquePaymentHash() {
        var claim = new dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim();
        claim.setId(UUID.randomUUID());
        claim.setWalletAddress("0xwallet");
        claim.setAmount(BigDecimal.ONE);
        claim.setClaimType("allocation");
        claim.setPaymentTransactionHash("0xpay");
        claim.setClaimedAtUtc(Instant.now());
        assertThat(claims.tryAdd(claim)).isTrue();
        var dup = new dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim();
        dup.setId(UUID.randomUUID());
        dup.setWalletAddress("0xwallet");
        dup.setAmount(BigDecimal.ONE);
        dup.setClaimType("allocation");
        dup.setPaymentTransactionHash("0xpay");
        dup.setClaimedAtUtc(Instant.now());
        assertThat(claims.tryAdd(dup)).isFalse();
        assertThat(claims.existsByPaymentHash("0xpay")).isTrue();
    }

    @Test
    void unitOfWorkCommitsOrRollsBack() {
        var uow = unitOfWork;
        try (var tx = uow.beginSerializableTransaction()) {
            ledger.addIfAbsent(entry("0xcommit", 0));
            tx.commit();
        }
        assertThat(ledger.existsByTransactionHash("0xcommit")).isTrue();
        try (var tx = uow.beginSerializableTransaction()) {
            ledger.addIfAbsent(entry("0xrollback", 0));
        }
        assertThat(ledger.existsByTransactionHash("0xrollback")).isFalse();
    }
}

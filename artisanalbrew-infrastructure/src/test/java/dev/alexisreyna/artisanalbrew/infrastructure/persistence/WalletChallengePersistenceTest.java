package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.domain.entities.WalletAuthChallenge;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaWalletAuthChallengeRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.WalletAuthChallengeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Single-use challenge lifecycle: exactly one of two consumes wins, and
 * pruning drops only long-expired/consumed rows in bounded batches.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class WalletChallengePersistenceTest {

    @Autowired
    private WalletAuthChallengeJpaRepository jpa;
    @Autowired
    private JpaWalletAuthChallengeRepositoryAdapter adapter;

    private static WalletAuthChallenge challenge(String nonce, Instant expires) {
        WalletAuthChallenge challenge = new WalletAuthChallenge();
        challenge.setId(UUID.randomUUID());
        challenge.setNonceHash(nonce);
        challenge.setPublicKey("0xpub");
        challenge.setChainKey("ethereum-sepolia");
        challenge.setOrigin("https://cafe.example");
        challenge.setMessageHash("0xmsg");
        challenge.setIssuedAtUtc(Instant.now());
        challenge.setExpiresAtUtc(expires);
        return challenge;
    }

    @Test
    void consumeIsSingleUseAndPruneIsBounded() {
        Instant now = Instant.now();
        adapter.add(challenge("n1", now.plusSeconds(300)));
        assertThat(adapter.tryConsume("n1", now)).isTrue();
        assertThat(adapter.tryConsume("n1", now)).isFalse();
        assertThat(adapter.findByNonceHash("n1").orElseThrow().getVerificationAttempts()).isEqualTo(1);

        adapter.add(challenge("old", now.minusSeconds(3600)));
        adapter.prune(now, 1000);
        assertThat(adapter.findByNonceHash("old")).isEmpty();
        assertThat(adapter.findByNonceHash("n1")).isPresent();
    }
}

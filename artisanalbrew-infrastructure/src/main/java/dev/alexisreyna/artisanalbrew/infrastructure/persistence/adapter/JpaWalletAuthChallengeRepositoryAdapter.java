package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.WalletAuthChallengeRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletAuthChallenge;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletAuthChallengeEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.WalletAuthChallengeJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** JPA adapter for {@link WalletAuthChallengeRepository}. */
@Repository
@Transactional
public class JpaWalletAuthChallengeRepositoryAdapter implements WalletAuthChallengeRepository {
    private final WalletAuthChallengeJpaRepository jpa;

    @PersistenceContext
    private EntityManager entities;

    public JpaWalletAuthChallengeRepositoryAdapter(WalletAuthChallengeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void prune(Instant now, int maxRows) {
        Instant expiredBefore = now.minus(Duration.ofMinutes(1));
        Instant consumedBefore = now.minus(Duration.ofMinutes(10));
        jpa.findByExpiresAtUtcBeforeOrConsumedAtUtcBefore(expiredBefore, consumedBefore).stream()
                .sorted((a, b) -> a.expiresAtUtc.compareTo(b.expiresAtUtc))
                .limit(maxRows)
                .forEach(row -> jpa.deleteById(row.id));
    }

    @Override
    public void add(WalletAuthChallenge challenge) {
        jpa.save(toEntity(challenge));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletAuthChallenge> findByNonceHash(String nonceHash) {
        return jpa.findByNonceHash(nonceHash).map(JpaWalletAuthChallengeRepositoryAdapter::toDomain);
    }

    @Override
    public boolean tryConsume(String nonceHash, Instant now) {
        int updated = jpa.tryConsume(nonceHash, now);
        if (updated == 1) {
            // Bulk updates bypass the persistence context (unlike EF's
            // ExecuteUpdate, which syncs tracked entities): refresh the row so
            // later reads in this transaction see ConsumedAtUtc/attempts.
            jpa.findByNonceHash(nonceHash).ifPresent(entities::refresh);
        }
        return updated == 1;
    }

    static WalletAuthChallengeEntity toEntity(WalletAuthChallenge challenge) {
        WalletAuthChallengeEntity e = new WalletAuthChallengeEntity();
        e.id = challenge.getId();
        e.nonceHash = challenge.getNonceHash();
        e.publicKey = challenge.getPublicKey();
        e.chainKey = challenge.getChainKey();
        e.origin = challenge.getOrigin();
        e.messageHash = challenge.getMessageHash();
        e.issuedAtUtc = challenge.getIssuedAtUtc();
        e.expiresAtUtc = challenge.getExpiresAtUtc();
        e.consumedAtUtc = challenge.getConsumedAtUtc();
        e.verificationAttempts = challenge.getVerificationAttempts();
        return e;
    }

    static WalletAuthChallenge toDomain(WalletAuthChallengeEntity e) {
        WalletAuthChallenge challenge = new WalletAuthChallenge();
        challenge.setId(e.id);
        challenge.setNonceHash(e.nonceHash);
        challenge.setPublicKey(e.publicKey);
        challenge.setChainKey(e.chainKey);
        challenge.setOrigin(e.origin);
        challenge.setMessageHash(e.messageHash);
        challenge.setIssuedAtUtc(e.issuedAtUtc);
        challenge.setExpiresAtUtc(e.expiresAtUtc);
        challenge.setConsumedAtUtc(e.consumedAtUtc);
        challenge.setVerificationAttempts(e.verificationAttempts);
        return challenge;
    }
}

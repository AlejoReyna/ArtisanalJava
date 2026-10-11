package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.WalletAuthChallenge;

import java.time.Instant;
import java.util.Optional;

/** Port of {@code Repositories/IWalletAuthChallengeRepository.cs}. */
public interface WalletAuthChallengeRepository {
    void prune(Instant now, int maxRows);

    default void prune(Instant now) {
        prune(now, 1000);
    }

    void add(WalletAuthChallenge challenge);

    Optional<WalletAuthChallenge> findByNonceHash(String nonceHash);

    /**
     * Atomically marks a challenge consumed. Must be a single conditional update,
     * not read-then-write — the atomicity is the whole point.
     */
    boolean tryConsume(String nonceHash, Instant now);
}

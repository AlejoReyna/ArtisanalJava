package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletAuthChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletAuthChallengeJpaRepository extends JpaRepository<WalletAuthChallengeEntity, UUID> {
    Optional<WalletAuthChallengeEntity> findByNonceHash(String nonceHash);

    /**
     * Single conditional UPDATE — the atomicity is the whole point (mirrors
     * {@code ExecuteUpdateAsync} with the {@code ConsumedAtUtc == null} guard).
     *
     * @return rows affected: exactly one of two concurrent callers sees 1.
     */
    @Modifying
    @Query("UPDATE WalletAuthChallengeEntity c SET c.consumedAtUtc = :now, "
            + "c.verificationAttempts = c.verificationAttempts + 1 "
            + "WHERE c.nonceHash = :nonceHash AND c.consumedAtUtc IS NULL AND c.expiresAtUtc > :now")
    int tryConsume(@Param("nonceHash") String nonceHash, @Param("now") Instant now);

    List<WalletAuthChallengeEntity> findByExpiresAtUtcBeforeOrConsumedAtUtcBefore(
            Instant expiredBefore, Instant consumedBefore);
}

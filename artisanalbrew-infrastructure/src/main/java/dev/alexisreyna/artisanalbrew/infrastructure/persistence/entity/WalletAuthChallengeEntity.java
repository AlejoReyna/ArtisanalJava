package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code WalletAuthChallenges}. */
@Entity
@Table(name = "\"WalletAuthChallenges\"")
public class WalletAuthChallengeEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"NonceHash\"")
    public String nonceHash;

    @Column(name = "\"PublicKey\"")
    public String publicKey;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"Origin\"")
    public String origin;

    @Column(name = "\"MessageHash\"")
    public String messageHash;

    @Column(name = "\"IssuedAtUtc\"")
    public Instant issuedAtUtc;

    @Column(name = "\"ExpiresAtUtc\"")
    public Instant expiresAtUtc;

    @Column(name = "\"ConsumedAtUtc\"")
    public Instant consumedAtUtc;

    @Column(name = "\"VerificationAttempts\"")
    public int verificationAttempts;
}

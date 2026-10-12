package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code SolanaFaucetClaims}. */
@Entity
@Table(name = "\"SolanaFaucetClaims\"")
public class SolanaFaucetClaimEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"Amount\"")
    public BigDecimal amount;

    @Column(name = "\"RawAmount\"")
    public long rawAmount;

    @Column(name = "\"Signature\"")
    public String signature;

    @Column(name = "\"ClaimedAtUtc\"")
    public Instant claimedAtUtc;
}

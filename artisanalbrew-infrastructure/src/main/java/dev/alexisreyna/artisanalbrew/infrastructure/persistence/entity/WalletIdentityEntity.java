package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code WalletIdentities}. */
@Entity
@Table(name = "\"WalletIdentities\"")
public class WalletIdentityEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"UserId\"")
    public String userId;

    @Column(name = "\"Family\"")
    public String family;

    @Column(name = "\"NormalizedAddress\"")
    public String normalizedAddress;

    @Column(name = "\"DisplayAddress\"")
    public String displayAddress;

    @Column(name = "\"WalletProvider\"")
    public String walletProvider;

    @Column(name = "\"VerifiedAtUtc\"")
    public Instant verifiedAtUtc;
}

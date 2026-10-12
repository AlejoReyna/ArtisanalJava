package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA row for {@code AspNetUsers}, fully mapped (partial mapping breaks
 * INSERTs on the NOT NULL Identity columns). Identity behavior itself
 * arrives with Phase 4; the profile adapter only reads/writes the link.
 */
@Entity
@Table(name = "\"AspNetUsers\"")
public class ApplicationUserEntity {
    @Id
    @Column(name = "\"Id\"")
    public String id;

    @Column(name = "\"AccessFailedCount\"")
    public int accessFailedCount;

    @Column(name = "\"ConcurrencyStamp\"")
    public String concurrencyStamp;

    @Column(name = "\"Email\"")
    public String email;

    @Column(name = "\"EmailConfirmed\"")
    public boolean emailConfirmed;

    @Column(name = "\"LockoutEnabled\"")
    public boolean lockoutEnabled;

    @Column(name = "\"LockoutEnd\"")
    public Instant lockoutEnd;

    @Column(name = "\"NormalizedEmail\"")
    public String normalizedEmail;

    @Column(name = "\"NormalizedUserName\"")
    public String normalizedUserName;

    @Column(name = "\"PasswordHash\"")
    public String passwordHash;

    @Column(name = "\"PhoneNumber\"")
    public String phoneNumber;

    @Column(name = "\"PhoneNumberConfirmed\"")
    public boolean phoneNumberConfirmed;

    @Column(name = "\"SecurityStamp\"")
    public String securityStamp;

    @Column(name = "\"TwoFactorEnabled\"")
    public boolean twoFactorEnabled;

    @Column(name = "\"UserName\"")
    public String userName;

    @Column(name = "\"UserProfileId\"")
    public UUID userProfileId;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"WalletChainId\"")
    public Integer walletChainId;

    @Column(name = "\"WalletVerifiedAt\"")
    public Instant walletVerifiedAt;
}

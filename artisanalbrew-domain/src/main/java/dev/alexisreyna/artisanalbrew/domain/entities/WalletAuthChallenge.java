package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/WalletAuthChallenge.cs}. */
public class WalletAuthChallenge {
    private UUID id = UUID.randomUUID();
    private String nonceHash = "";
    private String publicKey = "";
    private String chainKey = "";
    private String origin = "";
    private String messageHash = "";
    private Instant issuedAtUtc;
    private Instant expiresAtUtc;
    private Instant consumedAtUtc;
    private int verificationAttempts;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNonceHash() { return nonceHash; }
    public void setNonceHash(String v) { this.nonceHash = v; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String v) { this.publicKey = v; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getOrigin() { return origin; }
    public void setOrigin(String v) { this.origin = v; }
    public String getMessageHash() { return messageHash; }
    public void setMessageHash(String v) { this.messageHash = v; }
    public Instant getIssuedAtUtc() { return issuedAtUtc; }
    public void setIssuedAtUtc(Instant v) { this.issuedAtUtc = v; }
    public Instant getExpiresAtUtc() { return expiresAtUtc; }
    public void setExpiresAtUtc(Instant v) { this.expiresAtUtc = v; }
    public Instant getConsumedAtUtc() { return consumedAtUtc; }
    public void setConsumedAtUtc(Instant v) { this.consumedAtUtc = v; }
    public int getVerificationAttempts() { return verificationAttempts; }
    public void setVerificationAttempts(int v) { this.verificationAttempts = v; }
}

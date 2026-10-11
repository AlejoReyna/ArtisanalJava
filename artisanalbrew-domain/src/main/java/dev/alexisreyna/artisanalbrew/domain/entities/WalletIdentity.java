package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/WalletIdentity.cs}. */
public class WalletIdentity {
    private UUID id = UUID.randomUUID();
    private String userId = "";
    private String family = "";
    private String normalizedAddress = "";
    private String displayAddress = "";
    private String walletProvider = "";
    private Instant verifiedAtUtc;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    public String getNormalizedAddress() { return normalizedAddress; }
    public void setNormalizedAddress(String v) { this.normalizedAddress = v; }
    public String getDisplayAddress() { return displayAddress; }
    public void setDisplayAddress(String v) { this.displayAddress = v; }
    public String getWalletProvider() { return walletProvider; }
    public void setWalletProvider(String v) { this.walletProvider = v; }
    public Instant getVerifiedAtUtc() { return verifiedAtUtc; }
    public void setVerifiedAtUtc(Instant v) { this.verifiedAtUtc = v; }
}

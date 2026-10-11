package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code SolanaFaucetClaim.cs}. */
public class SolanaFaucetClaim {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String walletAddress = "";
    private BigDecimal amount = BigDecimal.ZERO;
    private long rawAmount;
    private String signature = "";
    private Instant claimedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String v) { this.walletAddress = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public long getRawAmount() { return rawAmount; }
    public void setRawAmount(long v) { this.rawAmount = v; }
    public String getSignature() { return signature; }
    public void setSignature(String v) { this.signature = v; }
    public Instant getClaimedAtUtc() { return claimedAtUtc; }
    public void setClaimedAtUtc(Instant v) { this.claimedAtUtc = v; }
}

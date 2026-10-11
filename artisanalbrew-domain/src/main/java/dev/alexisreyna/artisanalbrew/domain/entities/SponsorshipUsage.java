package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code SponsorshipUsage.cs}. */
public class SponsorshipUsage {
    private UUID id = UUID.randomUUID();
    private UUID grantId;
    private String chainKey = "";
    private String ownerAddress = "";
    private BigDecimal costUsd = BigDecimal.ZERO;
    private String targetAddress = "";
    private String selector = "";
    private Instant recordedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGrantId() { return grantId; }
    public void setGrantId(UUID v) { this.grantId = v; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String v) { this.ownerAddress = v; }
    public BigDecimal getCostUsd() { return costUsd; }
    public void setCostUsd(BigDecimal v) { this.costUsd = v; }
    public String getTargetAddress() { return targetAddress; }
    public void setTargetAddress(String v) { this.targetAddress = v; }
    public String getSelector() { return selector; }
    public void setSelector(String v) { this.selector = v; }
    public Instant getRecordedAtUtc() { return recordedAtUtc; }
    public void setRecordedAtUtc(Instant v) { this.recordedAtUtc = v; }
}

package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code CrossChainSolverCheckpoint.cs}. One row per (SourceChainKey, SourceResolverAddress). */
public class CrossChainSolverCheckpoint {
    private UUID id = UUID.randomUUID();
    private String sourceChainKey = "";
    private String sourceResolverAddress = "";
    private long lastScannedBlock;
    private Instant updatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSourceChainKey() { return sourceChainKey; }
    public void setSourceChainKey(String v) { this.sourceChainKey = v; }
    public String getSourceResolverAddress() { return sourceResolverAddress; }
    public void setSourceResolverAddress(String v) { this.sourceResolverAddress = v; }
    public long getLastScannedBlock() { return lastScannedBlock; }
    public void setLastScannedBlock(long v) { this.lastScannedBlock = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
}

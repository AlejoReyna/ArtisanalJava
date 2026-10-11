package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgenticCommerceReconciliationCheckpoint.cs}. One row per (ChainKey, EscrowAddress). */
public class AgenticCommerceReconciliationCheckpoint {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String escrowAddress = "";
    private long lastScannedBlock;
    private Instant updatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getEscrowAddress() { return escrowAddress; }
    public void setEscrowAddress(String v) { this.escrowAddress = v; }
    public long getLastScannedBlock() { return lastScannedBlock; }
    public void setLastScannedBlock(long v) { this.lastScannedBlock = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
}

package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code StakingReconciliationCheckpoint.cs}. */
public class StakingReconciliationCheckpoint {
    private UUID id = UUID.randomUUID();
    private String chainKey = "ethereum-sepolia";
    private String family = "Evm";
    private String sourceIdentifier = "";
    private String cursorType = "block";
    private String stakingPoolContract = "";
    private long lastScannedBlock;
    private long lastScannedSlot;
    private String lastScannedSignature = "";
    private Instant updatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getFamily() { return family; }
    public void setFamily(String v) { this.family = v; }
    public String getSourceIdentifier() { return sourceIdentifier; }
    public void setSourceIdentifier(String v) { this.sourceIdentifier = v; }
    public String getCursorType() { return cursorType; }
    public void setCursorType(String v) { this.cursorType = v; }
    public String getStakingPoolContract() { return stakingPoolContract; }
    public void setStakingPoolContract(String v) { this.stakingPoolContract = v; }
    public long getLastScannedBlock() { return lastScannedBlock; }
    public void setLastScannedBlock(long v) { this.lastScannedBlock = v; }
    public long getLastScannedSlot() { return lastScannedSlot; }
    public void setLastScannedSlot(long v) { this.lastScannedSlot = v; }
    public String getLastScannedSignature() { return lastScannedSignature; }
    public void setLastScannedSignature(String v) { this.lastScannedSignature = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
}

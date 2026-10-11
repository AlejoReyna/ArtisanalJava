package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgentPermissionEpoch} in {@code AgentPermissionEpoch.cs}. */
public class AgentPermissionEpoch {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private UUID smartAccountRecordId;
    private String delegatorAddress = "";
    private String ownerAddress = "";
    private String agentAddress = "";
    private String epoch = "";
    private Instant validAfterUtc;
    private Instant validBeforeUtc;
    private AgentPermissionEpochStatus status = AgentPermissionEpochStatus.PENDING_ACTIVATION;
    private Instant installedAtUtc;
    private String installedTxHash;
    private Instant revokedAtUtc;
    private String revokedTxHash;
    private Instant createdAtUtc = Instant.now();
    private Instant updatedAtUtc = Instant.now();
    private int concurrencyToken;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public UUID getSmartAccountRecordId() { return smartAccountRecordId; }
    public void setSmartAccountRecordId(UUID v) { this.smartAccountRecordId = v; }
    public String getDelegatorAddress() { return delegatorAddress; }
    public void setDelegatorAddress(String v) { this.delegatorAddress = v; }
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String v) { this.ownerAddress = v; }
    public String getAgentAddress() { return agentAddress; }
    public void setAgentAddress(String v) { this.agentAddress = v; }
    public String getEpoch() { return epoch; }
    public void setEpoch(String v) { this.epoch = v; }
    public Instant getValidAfterUtc() { return validAfterUtc; }
    public void setValidAfterUtc(Instant v) { this.validAfterUtc = v; }
    public Instant getValidBeforeUtc() { return validBeforeUtc; }
    public void setValidBeforeUtc(Instant v) { this.validBeforeUtc = v; }
    public AgentPermissionEpochStatus getStatus() { return status; }
    public void setStatus(AgentPermissionEpochStatus v) { this.status = v; }
    public Instant getInstalledAtUtc() { return installedAtUtc; }
    public void setInstalledAtUtc(Instant v) { this.installedAtUtc = v; }
    public String getInstalledTxHash() { return installedTxHash; }
    public void setInstalledTxHash(String v) { this.installedTxHash = v; }
    public Instant getRevokedAtUtc() { return revokedAtUtc; }
    public void setRevokedAtUtc(Instant v) { this.revokedAtUtc = v; }
    public String getRevokedTxHash() { return revokedTxHash; }
    public void setRevokedTxHash(String v) { this.revokedTxHash = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
    public int getConcurrencyToken() { return concurrencyToken; }
    public void setConcurrencyToken(int v) { this.concurrencyToken = v; }
}

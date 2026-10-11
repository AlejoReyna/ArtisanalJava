package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgentDirectoryEntry.cs}. */
public class AgentDirectoryEntry {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String registryAddress = "";
    private long agentId;
    private String ownerAddress = "";
    private String metadataUri = "";
    private boolean active;
    private Instant createdAtUtc = Instant.now();
    private Instant updatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getRegistryAddress() { return registryAddress; }
    public void setRegistryAddress(String v) { this.registryAddress = v; }
    public long getAgentId() { return agentId; }
    public void setAgentId(long v) { this.agentId = v; }
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String v) { this.ownerAddress = v; }
    public String getMetadataUri() { return metadataUri; }
    public void setMetadataUri(String v) { this.metadataUri = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean v) { this.active = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
}

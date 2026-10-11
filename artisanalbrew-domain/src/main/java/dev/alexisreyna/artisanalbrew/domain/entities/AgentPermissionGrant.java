package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgentPermissionGrant.cs}. */
public class AgentPermissionGrant {
    private UUID id = UUID.randomUUID();
    private UUID epochId;
    private String targetAddress = "";
    private String selector = "";
    private String tokenAddress;
    private String amountWei = "0";
    private String delegationHash = "";
    private String description = "";
    private Instant createdAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEpochId() { return epochId; }
    public void setEpochId(UUID v) { this.epochId = v; }
    public String getTargetAddress() { return targetAddress; }
    public void setTargetAddress(String v) { this.targetAddress = v; }
    public String getSelector() { return selector; }
    public void setSelector(String v) { this.selector = v; }
    public String getTokenAddress() { return tokenAddress; }
    public void setTokenAddress(String v) { this.tokenAddress = v; }
    public String getAmountWei() { return amountWei; }
    public void setAmountWei(String v) { this.amountWei = v; }
    public String getDelegationHash() { return delegationHash; }
    public void setDelegationHash(String v) { this.delegationHash = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
}

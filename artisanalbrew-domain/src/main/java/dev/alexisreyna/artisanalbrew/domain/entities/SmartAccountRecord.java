package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code SmartAccountRecord} in {@code SmartAccountRecord.cs}. */
public class SmartAccountRecord {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String ownerAddress = "";
    private SmartAccountType accountType;
    private String accountAddress = "";
    private String salt = "0";
    private String factoryAddress = "";
    private boolean deployed;
    private boolean implementationVerified;
    private Instant discoveredAtUtc = Instant.now();
    private Instant deployedAtUtc;
    private Instant updatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String v) { this.ownerAddress = v; }
    public SmartAccountType getAccountType() { return accountType; }
    public void setAccountType(SmartAccountType v) { this.accountType = v; }
    public String getAccountAddress() { return accountAddress; }
    public void setAccountAddress(String v) { this.accountAddress = v; }
    public String getSalt() { return salt; }
    public void setSalt(String v) { this.salt = v; }
    public String getFactoryAddress() { return factoryAddress; }
    public void setFactoryAddress(String v) { this.factoryAddress = v; }
    public boolean isDeployed() { return deployed; }
    public void setDeployed(boolean v) { this.deployed = v; }
    public boolean isImplementationVerified() { return implementationVerified; }
    public void setImplementationVerified(boolean v) { this.implementationVerified = v; }
    public Instant getDiscoveredAtUtc() { return discoveredAtUtc; }
    public void setDiscoveredAtUtc(Instant v) { this.discoveredAtUtc = v; }
    public Instant getDeployedAtUtc() { return deployedAtUtc; }
    public void setDeployedAtUtc(Instant v) { this.deployedAtUtc = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
}

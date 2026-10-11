package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgenticJobAppliedEvent.cs}. */
public class AgenticJobAppliedEvent {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String contractAddress = "";
    private String transactionHash = "";
    private int logIndex;
    private Instant appliedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String v) { this.contractAddress = v; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String v) { this.transactionHash = v; }
    public int getLogIndex() { return logIndex; }
    public void setLogIndex(int v) { this.logIndex = v; }
    public Instant getAppliedAtUtc() { return appliedAtUtc; }
    public void setAppliedAtUtc(Instant v) { this.appliedAtUtc = v; }
}

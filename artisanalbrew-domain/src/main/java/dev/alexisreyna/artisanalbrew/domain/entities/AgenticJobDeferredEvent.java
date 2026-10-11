package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code AgenticJobDeferredEvent.cs}. */
public class AgenticJobDeferredEvent {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String contractAddress = "";
    private long onChainJobId;
    private String eventType = "";
    private String transactionHash = "";
    private int logIndex;
    private long blockNumber;
    private String deferralReason = "";
    private Instant deferredAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String v) { this.contractAddress = v; }
    public long getOnChainJobId() { return onChainJobId; }
    public void setOnChainJobId(long v) { this.onChainJobId = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String v) { this.transactionHash = v; }
    public int getLogIndex() { return logIndex; }
    public void setLogIndex(int v) { this.logIndex = v; }
    public long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(long v) { this.blockNumber = v; }
    public String getDeferralReason() { return deferralReason; }
    public void setDeferralReason(String v) { this.deferralReason = v; }
    public Instant getDeferredAtUtc() { return deferredAtUtc; }
    public void setDeferredAtUtc(Instant v) { this.deferredAtUtc = v; }
}

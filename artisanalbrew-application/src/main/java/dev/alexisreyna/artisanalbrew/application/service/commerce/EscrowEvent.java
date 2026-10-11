package dev.alexisreyna.artisanalbrew.application.service.commerce;

import java.math.BigInteger;

/** Port of {@code EscrowEvent} in {@code AgenticCommerce/AgenticCommerceReconciliationApplicator.cs}. */
public final class EscrowEvent {
    private EscrowEventType type;
    private long onChainJobId;
    private String client = "";
    private String provider = "";
    private String evaluator = "";
    private BigInteger expiredAt = BigInteger.ZERO;
    private BigInteger amount = BigInteger.ZERO;
    private String deliverable = "";
    private String reason = "";
    private String transactionHash = "";
    private long blockNumber;
    private int logIndex;

    public EscrowEventType getType() { return type; }
    public void setType(EscrowEventType v) { this.type = v; }
    public long getOnChainJobId() { return onChainJobId; }
    public void setOnChainJobId(long v) { this.onChainJobId = v; }
    public String getClient() { return client; }
    public void setClient(String v) { this.client = v; }
    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getEvaluator() { return evaluator; }
    public void setEvaluator(String v) { this.evaluator = v; }
    public BigInteger getExpiredAt() { return expiredAt; }
    public void setExpiredAt(BigInteger v) { this.expiredAt = v; }
    public BigInteger getAmount() { return amount; }
    public void setAmount(BigInteger v) { this.amount = v; }
    public String getDeliverable() { return deliverable; }
    public void setDeliverable(String v) { this.deliverable = v; }
    public String getReason() { return reason; }
    public void setReason(String v) { this.reason = v; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String v) { this.transactionHash = v; }
    public long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(long v) { this.blockNumber = v; }
    public int getLogIndex() { return logIndex; }
    public void setLogIndex(int v) { this.logIndex = v; }
}

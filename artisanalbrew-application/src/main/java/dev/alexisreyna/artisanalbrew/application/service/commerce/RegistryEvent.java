package dev.alexisreyna.artisanalbrew.application.service.commerce;

/** Port of {@code RegistryEvent} in {@code AgenticCommerce/AgenticCommerceReconciliationApplicator.cs}. */
public final class RegistryEvent {
    private long agentId;
    private String owner = "";
    private String metadataUri = "";
    private String transactionHash = "";
    private long blockNumber;
    private int logIndex;

    public long getAgentId() { return agentId; }
    public void setAgentId(long v) { this.agentId = v; }
    public String getOwner() { return owner; }
    public void setOwner(String v) { this.owner = v; }
    public String getMetadataUri() { return metadataUri; }
    public void setMetadataUri(String v) { this.metadataUri = v; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String v) { this.transactionHash = v; }
    public long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(long v) { this.blockNumber = v; }
    public int getLogIndex() { return logIndex; }
    public void setLogIndex(int v) { this.logIndex = v; }
}

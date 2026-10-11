package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Port of {@code AgenticJobProjection.cs}.
 * {@code [ConcurrencyCheck] ConcurrencyToken} becomes {@code @Version} in Phase 3.
 */
public class AgenticJobProjection {
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_FUNDED = "Funded";
    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_EXPIRED = "Expired";

    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String escrowAddress = "";
    private long jobId;
    private String clientAddress = "";
    private String providerAddress = "";
    private String evaluatorAddress = "";
    private String descriptionCommitment = "";
    private BigDecimal budget = BigDecimal.ZERO;
    private long expiredAt;
    private String status = STATUS_OPEN;
    private String deliverableCommitment;
    private String decisionReason;
    private Instant createdAtUtc = Instant.now();
    private Instant updatedAtUtc = Instant.now();
    private long onChainJobId;
    private long chainId;
    private String contractAddress = "";
    private String creationTransactionHash = "";
    private String fundedTransactionHash = "";
    private String completionTransactionHash = "";
    private long lastReconciledBlock;
    private long concurrencyToken;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getEscrowAddress() { return escrowAddress; }
    public void setEscrowAddress(String v) { this.escrowAddress = v; }
    public long getJobId() { return jobId; }
    public void setJobId(long v) { this.jobId = v; }
    public String getClientAddress() { return clientAddress; }
    public void setClientAddress(String v) { this.clientAddress = v; }
    public String getProviderAddress() { return providerAddress; }
    public void setProviderAddress(String v) { this.providerAddress = v; }
    public String getEvaluatorAddress() { return evaluatorAddress; }
    public void setEvaluatorAddress(String v) { this.evaluatorAddress = v; }
    public String getDescriptionCommitment() { return descriptionCommitment; }
    public void setDescriptionCommitment(String v) { this.descriptionCommitment = v; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal v) { this.budget = v; }
    public long getExpiredAt() { return expiredAt; }
    public void setExpiredAt(long v) { this.expiredAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDeliverableCommitment() { return deliverableCommitment; }
    public void setDeliverableCommitment(String v) { this.deliverableCommitment = v; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String v) { this.decisionReason = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
    public long getOnChainJobId() { return onChainJobId; }
    public void setOnChainJobId(long v) { this.onChainJobId = v; }
    public long getChainId() { return chainId; }
    public void setChainId(long v) { this.chainId = v; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String v) { this.contractAddress = v; }
    public String getCreationTransactionHash() { return creationTransactionHash; }
    public void setCreationTransactionHash(String v) { this.creationTransactionHash = v; }
    public String getFundedTransactionHash() { return fundedTransactionHash; }
    public void setFundedTransactionHash(String v) { this.fundedTransactionHash = v; }
    public String getCompletionTransactionHash() { return completionTransactionHash; }
    public void setCompletionTransactionHash(String v) { this.completionTransactionHash = v; }
    public long getLastReconciledBlock() { return lastReconciledBlock; }
    public void setLastReconciledBlock(long v) { this.lastReconciledBlock = v; }
    public long getConcurrencyToken() { return concurrencyToken; }
    public void setConcurrencyToken(long v) { this.concurrencyToken = v; }
}

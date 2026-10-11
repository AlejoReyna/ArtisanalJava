package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code CrossChainSolverFill.cs}. Identity is (SourceChainKey, SourceResolverAddress, OrderId). */
public class CrossChainSolverFill {
    private UUID id = UUID.randomUUID();
    private String sourceChainKey = "";
    private String sourceResolverAddress = "";
    private String orderId = "";
    private String submitTransactionHash = "";
    private boolean filled;
    private String fillTransactionHash;
    private String denialReason;
    private Instant evaluatedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSourceChainKey() { return sourceChainKey; }
    public void setSourceChainKey(String v) { this.sourceChainKey = v; }
    public String getSourceResolverAddress() { return sourceResolverAddress; }
    public void setSourceResolverAddress(String v) { this.sourceResolverAddress = v; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String v) { this.orderId = v; }
    public String getSubmitTransactionHash() { return submitTransactionHash; }
    public void setSubmitTransactionHash(String v) { this.submitTransactionHash = v; }
    public boolean isFilled() { return filled; }
    public void setFilled(boolean v) { this.filled = v; }
    public String getFillTransactionHash() { return fillTransactionHash; }
    public void setFillTransactionHash(String v) { this.fillTransactionHash = v; }
    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String v) { this.denialReason = v; }
    public Instant getEvaluatedAtUtc() { return evaluatedAtUtc; }
    public void setEvaluatedAtUtc(Instant v) { this.evaluatedAtUtc = v; }
}

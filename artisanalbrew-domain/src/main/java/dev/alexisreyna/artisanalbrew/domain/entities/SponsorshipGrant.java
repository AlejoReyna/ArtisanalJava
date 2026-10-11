package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Port of {@code SponsorshipGrant.cs}.
 * {@code [ConcurrencyCheck] ConcurrencyToken} becomes a {@code @Version} field in Phase 3.
 */
public class SponsorshipGrant {
    private UUID id = UUID.randomUUID();
    private String chainKey = "";
    private String ownerAddress = "";
    private BigDecimal budgetUsd = BigDecimal.ZERO;
    private BigDecimal spentUsd = BigDecimal.ZERO;
    private BigDecimal maxOperationCostUsd = BigDecimal.ZERO;
    private Instant validFromUtc = Instant.now();
    private Instant validUntilUtc;
    private Instant revokedAtUtc;
    private int revertedOperationCount;
    private Instant createdAtUtc = Instant.now();
    private Instant updatedAtUtc = Instant.now();
    private int concurrencyToken;

    public BigDecimal getRemainingUsd() {
        return budgetUsd.subtract(spentUsd);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String v) { this.ownerAddress = v; }
    public BigDecimal getBudgetUsd() { return budgetUsd; }
    public void setBudgetUsd(BigDecimal v) { this.budgetUsd = v; }
    public BigDecimal getSpentUsd() { return spentUsd; }
    public void setSpentUsd(BigDecimal v) { this.spentUsd = v; }
    public BigDecimal getMaxOperationCostUsd() { return maxOperationCostUsd; }
    public void setMaxOperationCostUsd(BigDecimal v) { this.maxOperationCostUsd = v; }
    public Instant getValidFromUtc() { return validFromUtc; }
    public void setValidFromUtc(Instant v) { this.validFromUtc = v; }
    public Instant getValidUntilUtc() { return validUntilUtc; }
    public void setValidUntilUtc(Instant v) { this.validUntilUtc = v; }
    public Instant getRevokedAtUtc() { return revokedAtUtc; }
    public void setRevokedAtUtc(Instant v) { this.revokedAtUtc = v; }
    public int getRevertedOperationCount() { return revertedOperationCount; }
    public void setRevertedOperationCount(int v) { this.revertedOperationCount = v; }
    public Instant getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(Instant v) { this.createdAtUtc = v; }
    public Instant getUpdatedAtUtc() { return updatedAtUtc; }
    public void setUpdatedAtUtc(Instant v) { this.updatedAtUtc = v; }
    public int getConcurrencyToken() { return concurrencyToken; }
    public void setConcurrencyToken(int v) { this.concurrencyToken = v; }
}

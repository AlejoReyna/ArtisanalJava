package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;
import java.time.Instant;

/** Port of {@code Blockchain/CafeFaucetStatus.cs}. */
public final class CafeFaucetStatus {
    private boolean configured;
    private BigDecimal claimAmount = BigDecimal.ZERO;
    private int cooldownSeconds;
    private Instant nextClaimAtUtc;
    private boolean canClaim;
    private BigDecimal faucetBalance = BigDecimal.ZERO;

    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean v) { this.configured = v; }
    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal v) { this.claimAmount = v; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int v) { this.cooldownSeconds = v; }
    public Instant getNextClaimAtUtc() { return nextClaimAtUtc; }
    public void setNextClaimAtUtc(Instant v) { this.nextClaimAtUtc = v; }
    public boolean isCanClaim() { return canClaim; }
    public void setCanClaim(boolean v) { this.canClaim = v; }
    public BigDecimal getFaucetBalance() { return faucetBalance; }
    public void setFaucetBalance(BigDecimal v) { this.faucetBalance = v; }
}

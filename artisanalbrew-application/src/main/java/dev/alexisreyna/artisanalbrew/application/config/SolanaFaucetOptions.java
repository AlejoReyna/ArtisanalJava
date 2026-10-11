package dev.alexisreyna.artisanalbrew.application.config;

import java.math.BigDecimal;

/** Port of {@code Configuration/SolanaFaucetOptions.cs}. */
public final class SolanaFaucetOptions {
    public static final String SECTION_NAME = "Blockchain:SolanaFaucet";

    private BigDecimal claimAmount = new BigDecimal("100");
    private int cooldownSeconds = 86_400;

    public BigDecimal getClaimAmount() { return claimAmount; }
    public void setClaimAmount(BigDecimal v) { this.claimAmount = v; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int v) { this.cooldownSeconds = v; }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Port of {@code LiquidStakingDashboard} in {@code Blockchain/LiquidStakingModels.cs}. */
public final class LiquidStakingDashboard {
    private String chainKey = "";
    private String family = "";
    private String walletIdentifier = "";
    private boolean configured;
    private String unavailableReason;
    private BigDecimal cafeBalance = BigDecimal.ZERO;
    private BigDecimal stCafeBalance = BigDecimal.ZERO;
    private BigDecimal redeemableCafe = BigDecimal.ZERO;
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private BigDecimal pendingCoffee = BigDecimal.ZERO;
    private BigDecimal coffeeBalance = BigDecimal.ZERO;
    private BigDecimal depositPreviewShares = BigDecimal.ZERO;
    private BigDecimal redeemPreviewCafe = BigDecimal.ZERO;
    private BigDecimal nativeGasBalance = BigDecimal.ZERO;
    private String vaultIdentifier = "";
    private String assetIdentifier = "";
    private String receiptIdentifier = "";
    private String rewardIdentifier = "";
    private OffsetDateTime readAtUtc = OffsetDateTime.now();

    public String getChainKey() { return chainKey; }
    public void setChainKey(String v) { this.chainKey = v; }
    public String getFamily() { return family; }
    public void setFamily(String v) { this.family = v; }
    public String getWalletIdentifier() { return walletIdentifier; }
    public void setWalletIdentifier(String v) { this.walletIdentifier = v; }
    public boolean isConfigured() { return configured; }
    public void setConfigured(boolean v) { this.configured = v; }
    public String getUnavailableReason() { return unavailableReason; }
    public void setUnavailableReason(String v) { this.unavailableReason = v; }
    public BigDecimal getCafeBalance() { return cafeBalance; }
    public void setCafeBalance(BigDecimal v) { this.cafeBalance = v; }
    public BigDecimal getStCafeBalance() { return stCafeBalance; }
    public void setStCafeBalance(BigDecimal v) { this.stCafeBalance = v; }
    public BigDecimal getRedeemableCafe() { return redeemableCafe; }
    public void setRedeemableCafe(BigDecimal v) { this.redeemableCafe = v; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal v) { this.exchangeRate = v; }
    public BigDecimal getPendingCoffee() { return pendingCoffee; }
    public void setPendingCoffee(BigDecimal v) { this.pendingCoffee = v; }
    public BigDecimal getCoffeeBalance() { return coffeeBalance; }
    public void setCoffeeBalance(BigDecimal v) { this.coffeeBalance = v; }
    public BigDecimal getDepositPreviewShares() { return depositPreviewShares; }
    public void setDepositPreviewShares(BigDecimal v) { this.depositPreviewShares = v; }
    public BigDecimal getRedeemPreviewCafe() { return redeemPreviewCafe; }
    public void setRedeemPreviewCafe(BigDecimal v) { this.redeemPreviewCafe = v; }
    public BigDecimal getNativeGasBalance() { return nativeGasBalance; }
    public void setNativeGasBalance(BigDecimal v) { this.nativeGasBalance = v; }
    public String getVaultIdentifier() { return vaultIdentifier; }
    public void setVaultIdentifier(String v) { this.vaultIdentifier = v; }
    public String getAssetIdentifier() { return assetIdentifier; }
    public void setAssetIdentifier(String v) { this.assetIdentifier = v; }
    public String getReceiptIdentifier() { return receiptIdentifier; }
    public void setReceiptIdentifier(String v) { this.receiptIdentifier = v; }
    public String getRewardIdentifier() { return rewardIdentifier; }
    public void setRewardIdentifier(String v) { this.rewardIdentifier = v; }
    public OffsetDateTime getReadAtUtc() { return readAtUtc; }
    public void setReadAtUtc(OffsetDateTime v) { this.readAtUtc = v; }
}

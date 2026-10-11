package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardCalculator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Port of {@code Blockchain/CoffeeDashboardModel.cs}. */
public final class CoffeeDashboardModel {
    private String walletAddress = "";
    private BigDecimal nativeBalance = BigDecimal.ZERO;
    private BigDecimal paymentTokenBalance = BigDecimal.ZERO;
    private BigDecimal stakedPaymentTokenBalance = BigDecimal.ZERO;
    private BigDecimal pendingStakingRewards;
    private BigDecimal coffeeCoinBalance = BigDecimal.ZERO;
    private BigDecimal currentApr = BigDecimal.ZERO;
    private boolean aprIsContractDerived;
    private String claimFunctionName;

    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String v) { this.walletAddress = v; }
    public BigDecimal getNativeBalance() { return nativeBalance; }
    public void setNativeBalance(BigDecimal v) { this.nativeBalance = v; }
    public BigDecimal getPaymentTokenBalance() { return paymentTokenBalance; }
    public void setPaymentTokenBalance(BigDecimal v) { this.paymentTokenBalance = v; }
    public BigDecimal getStakedPaymentTokenBalance() { return stakedPaymentTokenBalance; }
    public void setStakedPaymentTokenBalance(BigDecimal v) { this.stakedPaymentTokenBalance = v; }
    public BigDecimal getPendingStakingRewards() { return pendingStakingRewards; }
    public void setPendingStakingRewards(BigDecimal v) { this.pendingStakingRewards = v; }
    public BigDecimal getCoffeeCoinBalance() { return coffeeCoinBalance; }
    public void setCoffeeCoinBalance(BigDecimal v) { this.coffeeCoinBalance = v; }
    public BigDecimal getCurrentApr() { return currentApr; }
    public void setCurrentApr(BigDecimal v) { this.currentApr = v; }
    public boolean isAprIsContractDerived() { return aprIsContractDerived; }
    public void setAprIsContractDerived(boolean v) { this.aprIsContractDerived = v; }
    public String getClaimFunctionName() { return claimFunctionName; }
    public void setClaimFunctionName(String v) { this.claimFunctionName = v; }

    public boolean isClaimSupported() {
        return claimFunctionName != null && !claimFunctionName.isEmpty();
    }

    public BigDecimal getEstimatedDailyReward() {
        return RewardCalculator.dailyRewardFromPaymentToken(stakedPaymentTokenBalance, currentApr);
    }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;
import java.time.Instant;

/** Port of {@code RewardClaimStatusModel} + {@code RewardClaimResultModel} in {@code Blockchain/RewardClaimStatusModel.cs}. */
public final class RewardClaimModels {
    private RewardClaimModels() {
    }

    public static final class Status {
        private String walletAddress = "";
        private BigDecimal paymentTokenBalance = BigDecimal.ZERO;
        private BigDecimal estimatedDailyReward = BigDecimal.ZERO;
        private BigDecimal claimableAmount = BigDecimal.ZERO;
        private boolean canClaimToday;
        private boolean mintingEnabled;
        private Instant lastClaimedAtUtc;

        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String v) { this.walletAddress = v; }
        public BigDecimal getPaymentTokenBalance() { return paymentTokenBalance; }
        public void setPaymentTokenBalance(BigDecimal v) { this.paymentTokenBalance = v; }
        public BigDecimal getEstimatedDailyReward() { return estimatedDailyReward; }
        public void setEstimatedDailyReward(BigDecimal v) { this.estimatedDailyReward = v; }
        public BigDecimal getClaimableAmount() { return claimableAmount; }
        public void setClaimableAmount(BigDecimal v) { this.claimableAmount = v; }
        public boolean isCanClaimToday() { return canClaimToday; }
        public void setCanClaimToday(boolean v) { this.canClaimToday = v; }
        public boolean isMintingEnabled() { return mintingEnabled; }
        public void setMintingEnabled(boolean v) { this.mintingEnabled = v; }
        public Instant getLastClaimedAtUtc() { return lastClaimedAtUtc; }
        public void setLastClaimedAtUtc(Instant v) { this.lastClaimedAtUtc = v; }
    }

    public static final class Result {
        private boolean success;
        private String transactionHash;
        private String paymentTransactionHash;
        private BigDecimal mintedAmount = BigDecimal.ZERO;
        private String error;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean v) { this.success = v; }
        public String getTransactionHash() { return transactionHash; }
        public void setTransactionHash(String v) { this.transactionHash = v; }
        public String getPaymentTransactionHash() { return paymentTransactionHash; }
        public void setPaymentTransactionHash(String v) { this.paymentTransactionHash = v; }
        public BigDecimal getMintedAmount() { return mintedAmount; }
        public void setMintedAmount(BigDecimal v) { this.mintedAmount = v; }
        public String getError() { return error; }
        public void setError(String v) { this.error = v; }
    }
}

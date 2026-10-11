package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/RewardClaim.cs}. */
public class RewardClaim {
    private UUID id = UUID.randomUUID();
    private String walletAddress = "";
    private BigDecimal amount = BigDecimal.ZERO;
    private String claimType = "daily";
    private String transactionHash;
    private String paymentTransactionHash;
    private BigDecimal paymentAmount;
    private Integer paymentChainId;
    private String paymentNetworkName;
    private String paymentTokenContract;
    private String marketplaceWallet;
    private String allocationName;
    private String paymentExplorerUrl;
    private String mintExplorerUrl;
    private Instant claimedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    public String getPaymentTransactionHash() { return paymentTransactionHash; }
    public void setPaymentTransactionHash(String v) { this.paymentTransactionHash = v; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal v) { this.paymentAmount = v; }
    public Integer getPaymentChainId() { return paymentChainId; }
    public void setPaymentChainId(Integer v) { this.paymentChainId = v; }
    public String getPaymentNetworkName() { return paymentNetworkName; }
    public void setPaymentNetworkName(String v) { this.paymentNetworkName = v; }
    public String getPaymentTokenContract() { return paymentTokenContract; }
    public void setPaymentTokenContract(String v) { this.paymentTokenContract = v; }
    public String getMarketplaceWallet() { return marketplaceWallet; }
    public void setMarketplaceWallet(String v) { this.marketplaceWallet = v; }
    public String getAllocationName() { return allocationName; }
    public void setAllocationName(String v) { this.allocationName = v; }
    public String getPaymentExplorerUrl() { return paymentExplorerUrl; }
    public void setPaymentExplorerUrl(String v) { this.paymentExplorerUrl = v; }
    public String getMintExplorerUrl() { return mintExplorerUrl; }
    public void setMintExplorerUrl(String v) { this.mintExplorerUrl = v; }
    public Instant getClaimedAtUtc() { return claimedAtUtc; }
    public void setClaimedAtUtc(Instant v) { this.claimedAtUtc = v; }
}

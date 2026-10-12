package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code RewardClaims}. */
@Entity
@Table(name = "\"RewardClaims\"")
public class RewardClaimEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"Amount\"")
    public BigDecimal amount;

    @Column(name = "\"ClaimType\"")
    public String claimType;

    @Column(name = "\"TransactionHash\"")
    public String transactionHash;

    @Column(name = "\"PaymentTransactionHash\"")
    public String paymentTransactionHash;

    @Column(name = "\"PaymentAmount\"")
    public BigDecimal paymentAmount;

    @Column(name = "\"PaymentChainId\"")
    public Integer paymentChainId;

    @Column(name = "\"PaymentNetworkName\"")
    public String paymentNetworkName;

    @Column(name = "\"PaymentTokenContract\"")
    public String paymentTokenContract;

    @Column(name = "\"MarketplaceWallet\"")
    public String marketplaceWallet;

    @Column(name = "\"AllocationName\"")
    public String allocationName;

    @Column(name = "\"PaymentExplorerUrl\"")
    public String paymentExplorerUrl;

    @Column(name = "\"MintExplorerUrl\"")
    public String mintExplorerUrl;

    @Column(name = "\"ClaimedAtUtc\"")
    public Instant claimedAtUtc;
}

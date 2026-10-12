package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code StakingLedgerEntries}. */
@Entity
@Table(name = "\"StakingLedgerEntries\"")
public class StakingLedgerEntryEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"Family\"")
    public String family;

    @Column(name = "\"ActionType\"")
    public String actionType;

    @Column(name = "\"Amount\"")
    public BigDecimal amount;

    @Column(name = "\"AssetAmount\"")
    public BigDecimal assetAmount;

    @Column(name = "\"ShareAmount\"")
    public BigDecimal shareAmount;

    @Column(name = "\"RewardAmount\"")
    public BigDecimal rewardAmount;

    @Column(name = "\"RawAssetAmount\"")
    public String rawAssetAmount;

    @Column(name = "\"RawShareAmount\"")
    public String rawShareAmount;

    @Column(name = "\"RawRewardAmount\"")
    public String rawRewardAmount;

    @Column(name = "\"TransactionHash\"")
    public String transactionHash;

    @Column(name = "\"OperationIndex\"")
    public int operationIndex;

    @Column(name = "\"ChainId\"")
    public int chainId;

    @Column(name = "\"NetworkName\"")
    public String networkName;

    @Column(name = "\"PaymentTokenContract\"")
    public String paymentTokenContract;

    @Column(name = "\"StakingPoolContract\"")
    public String stakingPoolContract;

    @Column(name = "\"AssetIdentifier\"")
    public String assetIdentifier;

    @Column(name = "\"ReceiptIdentifier\"")
    public String receiptIdentifier;

    @Column(name = "\"RewardIdentifier\"")
    public String rewardIdentifier;

    @Column(name = "\"VaultOrProgramIdentifier\"")
    public String vaultOrProgramIdentifier;

    @Column(name = "\"BlockOrSlot\"")
    public long blockOrSlot;

    @Column(name = "\"VerificationState\"")
    public String verificationState;

    @Column(name = "\"Verified\"")
    public boolean verified;

    @Column(name = "\"OccurredAtUtc\"")
    public Instant occurredAtUtc;

    @Column(name = "\"ExplorerUrl\"")
    public String explorerUrl;

    @Column(name = "\"RecordedAtUtc\"")
    public Instant recordedAtUtc;
}

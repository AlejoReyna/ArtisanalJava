package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

/** Port of {@code StakingLedgerEntry} in {@code StakingLedgerEntry.cs}. */
public class StakingLedgerEntry {
    private UUID id = UUID.randomUUID();
    private String walletAddress = "";
    private String chainKey = "ethereum-sepolia";
    private String family = "Evm";
    private String actionType = "";
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal assetAmount = BigDecimal.ZERO;
    private BigDecimal shareAmount = BigDecimal.ZERO;
    private BigDecimal rewardAmount = BigDecimal.ZERO;
    private String rawAssetAmount = "0";
    private String rawShareAmount = "0";
    private String rawRewardAmount = "0";
    private String transactionHash = "";
    private int operationIndex;
    private int chainId;
    private String networkName = "";
    private String paymentTokenContract = "";
    private String stakingPoolContract = "";
    private String assetIdentifier = "";
    private String receiptIdentifier = "";
    private String rewardIdentifier = "";
    private String vaultOrProgramIdentifier = "";
    private long blockOrSlot;
    private String verificationState = "verified";
    private boolean verified;
    private Instant occurredAtUtc;
    private String explorerUrl = "";
    private Instant recordedAtUtc = Instant.now();

    public static StakingLedgerEntry create(String chainKey, String transactionHash, int operationIndex) {
        return create(chainKey, transactionHash, operationIndex, null);
    }

    public static StakingLedgerEntry create(String chainKey, String transactionHash, int operationIndex,
                                            Consumer<StakingLedgerEntry> configure) {
        StakingOperationIdentity identity = StakingOperationIdentity.create(chainKey, transactionHash, operationIndex);
        StakingLedgerEntry entry = new StakingLedgerEntry();
        entry.chainKey = identity.chainKey();
        entry.transactionHash = identity.transactionHash();
        entry.operationIndex = identity.operationIndex();
        if (configure != null) {
            configure.accept(entry);
        }
        return entry;
    }

    public StakingOperationIdentity getOperationIdentity() {
        return StakingOperationIdentity.create(chainKey, transactionHash, operationIndex);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String v) { this.walletAddress = v; }
    public String getChainKey() { return chainKey; }
    public String getFamily() { return family; }
    public void setFamily(String v) { this.family = v; }
    public String getActionType() { return actionType; }
    public void setActionType(String v) { this.actionType = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public BigDecimal getAssetAmount() { return assetAmount; }
    public void setAssetAmount(BigDecimal v) { this.assetAmount = v; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal v) { this.shareAmount = v; }
    public BigDecimal getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(BigDecimal v) { this.rewardAmount = v; }
    public String getRawAssetAmount() { return rawAssetAmount; }
    public void setRawAssetAmount(String v) { this.rawAssetAmount = v; }
    public String getRawShareAmount() { return rawShareAmount; }
    public void setRawShareAmount(String v) { this.rawShareAmount = v; }
    public String getRawRewardAmount() { return rawRewardAmount; }
    public void setRawRewardAmount(String v) { this.rawRewardAmount = v; }
    public String getTransactionHash() { return transactionHash; }
    public int getOperationIndex() { return operationIndex; }
    public int getChainId() { return chainId; }
    public void setChainId(int v) { this.chainId = v; }
    public String getNetworkName() { return networkName; }
    public void setNetworkName(String v) { this.networkName = v; }
    public String getPaymentTokenContract() { return paymentTokenContract; }
    public void setPaymentTokenContract(String v) { this.paymentTokenContract = v; }
    public String getStakingPoolContract() { return stakingPoolContract; }
    public void setStakingPoolContract(String v) { this.stakingPoolContract = v; }
    public String getAssetIdentifier() { return assetIdentifier; }
    public void setAssetIdentifier(String v) { this.assetIdentifier = v; }
    public String getReceiptIdentifier() { return receiptIdentifier; }
    public void setReceiptIdentifier(String v) { this.receiptIdentifier = v; }
    public String getRewardIdentifier() { return rewardIdentifier; }
    public void setRewardIdentifier(String v) { this.rewardIdentifier = v; }
    public String getVaultOrProgramIdentifier() { return vaultOrProgramIdentifier; }
    public void setVaultOrProgramIdentifier(String v) { this.vaultOrProgramIdentifier = v; }
    public long getBlockOrSlot() { return blockOrSlot; }
    public void setBlockOrSlot(long v) { this.blockOrSlot = v; }
    public String getVerificationState() { return verificationState; }
    public void setVerificationState(String v) { this.verificationState = v; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean v) { this.verified = v; }
    public Instant getOccurredAtUtc() { return occurredAtUtc; }
    public void setOccurredAtUtc(Instant v) { this.occurredAtUtc = v; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String v) { this.explorerUrl = v; }
    public Instant getRecordedAtUtc() { return recordedAtUtc; }
    public void setRecordedAtUtc(Instant v) { this.recordedAtUtc = v; }
}

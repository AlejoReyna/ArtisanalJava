package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerRepository;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerWriteResult;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.StakingLedgerEntryEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.StakingLedgerEntryJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for {@link StakingLedgerRepository}. Short-lived, one-operation
 * transactions mirror the .NET {@code IDbContextFactory} usage: safe from
 * Blazor-circuit-style long-lived callers as well as request scopes.
 */
@Repository
@Transactional
public class JpaStakingLedgerRepositoryAdapter implements StakingLedgerRepository {
    private final StakingLedgerEntryJpaRepository jpa;

    @PersistenceContext
    private EntityManager entities;

    public JpaStakingLedgerRepositoryAdapter(StakingLedgerEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTransactionHash(String transactionHash) {
        return jpa.existsByTransactionHash(transactionHash);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StakingLedgerEntry> findByOperation(String chainKey, String transactionHash, int operationIndex) {
        return jpa.findByChainKeyAndTransactionHashAndOperationIndex(chainKey, transactionHash, operationIndex)
                .map(JpaStakingLedgerRepositoryAdapter::toDomain);
    }

    @Override
    public StakingLedgerWriteResult addIfAbsent(StakingLedgerEntry entry) {
        var identity = entry.getOperationIdentity();
        Optional<StakingLedgerEntry> existing =
                findByOperation(identity.chainKey(), identity.transactionHash(), identity.operationIndex());
        if (existing.isPresent()) {
            return StakingLedgerWriteResult.alreadyPresent(existing.get());
        }
        StakingLedgerEntryEntity row = toEntity(entry);
        try {
            jpa.saveAndFlush(row);
            return StakingLedgerWriteResult.written();
        } catch (jakarta.persistence.PersistenceException | org.springframework.dao.DataAccessException e) {
            // Either the raw Hibernate failure or Spring's translated
            // DataIntegrityViolationException (see reward adapter note).
            // Lost a race: the unique (ChainKey, TransactionHash, OperationIndex)
            // index kept the ledger honest. A failed Hibernate flush leaves the
            // insert queued (detach is not enough), so the context is cleared
            // — the EF equivalent of detaching the entry — then re-read to
            // answer idempotently.
            entities.clear();
            Optional<StakingLedgerEntry> concurrent =
                    findByOperation(identity.chainKey(), identity.transactionHash(), identity.operationIndex());
            return concurrent.map(StakingLedgerWriteResult::alreadyPresent)
                    .orElseGet(StakingLedgerWriteResult::conflict);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StakingLedgerEntry> listByWallet(String walletAddress, int take) {
        return jpa.findByWalletAddressOrderByRecordedAtUtcDesc(walletAddress).stream().limit(take)
                .map(JpaStakingLedgerRepositoryAdapter::toDomain).toList();
    }

    static StakingLedgerEntryEntity toEntity(StakingLedgerEntry entry) {
        StakingLedgerEntryEntity e = new StakingLedgerEntryEntity();
        e.id = entry.getId();
        e.walletAddress = entry.getWalletAddress();
        e.chainKey = entry.getChainKey();
        e.family = entry.getFamily();
        e.actionType = entry.getActionType();
        e.amount = entry.getAmount();
        e.assetAmount = entry.getAssetAmount();
        e.shareAmount = entry.getShareAmount();
        e.rewardAmount = entry.getRewardAmount();
        e.rawAssetAmount = entry.getRawAssetAmount();
        e.rawShareAmount = entry.getRawShareAmount();
        e.rawRewardAmount = entry.getRawRewardAmount();
        e.transactionHash = entry.getTransactionHash();
        e.operationIndex = entry.getOperationIndex();
        e.chainId = entry.getChainId();
        e.networkName = entry.getNetworkName();
        e.paymentTokenContract = entry.getPaymentTokenContract();
        e.stakingPoolContract = entry.getStakingPoolContract();
        e.assetIdentifier = entry.getAssetIdentifier();
        e.receiptIdentifier = entry.getReceiptIdentifier();
        e.rewardIdentifier = entry.getRewardIdentifier();
        e.vaultOrProgramIdentifier = entry.getVaultOrProgramIdentifier();
        e.blockOrSlot = entry.getBlockOrSlot();
        e.verificationState = entry.getVerificationState();
        e.verified = entry.isVerified();
        e.occurredAtUtc = entry.getOccurredAtUtc();
        e.explorerUrl = entry.getExplorerUrl();
        e.recordedAtUtc = entry.getRecordedAtUtc();
        return e;
    }

    static StakingLedgerEntry toDomain(StakingLedgerEntryEntity e) {
        StakingLedgerEntry entry = StakingLedgerEntry.create(e.chainKey, e.transactionHash, e.operationIndex);
        entry.setId(e.id);
        entry.setWalletAddress(e.walletAddress);
        entry.setFamily(e.family);
        entry.setActionType(e.actionType);
        entry.setAmount(e.amount);
        entry.setAssetAmount(e.assetAmount);
        entry.setShareAmount(e.shareAmount);
        entry.setRewardAmount(e.rewardAmount);
        entry.setRawAssetAmount(e.rawAssetAmount);
        entry.setRawShareAmount(e.rawShareAmount);
        entry.setRawRewardAmount(e.rawRewardAmount);
        entry.setChainId(e.chainId);
        entry.setNetworkName(e.networkName);
        entry.setPaymentTokenContract(e.paymentTokenContract);
        entry.setStakingPoolContract(e.stakingPoolContract);
        entry.setAssetIdentifier(e.assetIdentifier);
        entry.setReceiptIdentifier(e.receiptIdentifier);
        entry.setRewardIdentifier(e.rewardIdentifier);
        entry.setVaultOrProgramIdentifier(e.vaultOrProgramIdentifier);
        entry.setBlockOrSlot(e.blockOrSlot);
        entry.setVerificationState(e.verificationState);
        entry.setVerified(e.verified);
        entry.setOccurredAtUtc(e.occurredAtUtc);
        entry.setExplorerUrl(e.explorerUrl);
        entry.setRecordedAtUtc(e.recordedAtUtc);
        return entry;
    }
}

package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.RewardClaimEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RewardClaimJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for {@link RewardClaimRepository}. {@code tryAdd} relies on the
 * filtered unique index on {@code PaymentTransactionHash}, like .NET's
 * {@code DbUpdateException} catch — the constraint, not the pre-check, is real.
 */
@Repository
@Transactional
public class JpaRewardClaimRepositoryAdapter implements RewardClaimRepository {
    private final RewardClaimJpaRepository jpa;

    @PersistenceContext
    private EntityManager entities;

    public JpaRewardClaimRepositoryAdapter(RewardClaimJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RewardClaim> getLatestDailyClaim(String walletAddress) {
        return jpa.findFirstByWalletAddressAndClaimTypeOrderByClaimedAtUtcDesc(walletAddress, "daily")
                .map(JpaRewardClaimRepositoryAdapter::toDomain);
    }

    @Override
    public void add(RewardClaim claim) {
        jpa.save(toEntity(claim));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPaymentHash(String paymentTransactionHash) {
        if (paymentTransactionHash == null) {
            return false;
        }
        return jpa.existsByPaymentTransactionHash(paymentTransactionHash);
    }

    @Override
    public boolean tryAdd(RewardClaim claim) {
        RewardClaimEntity row = toEntity(claim);
        try {
            jpa.saveAndFlush(row);
            return true;
        } catch (jakarta.persistence.PersistenceException | org.springframework.dao.DataAccessException e) {
            // Either the raw Hibernate failure or Spring's translated
            // DataIntegrityViolationException: translation happens at the
            // repository proxy, so both shapes escape saveAndFlush in practice.
            // A failed Hibernate flush leaves the insert queued: detach() is
            // not enough (the action re-executes on the next flush), so the
            // context is cleared — the EF equivalent of detaching the entry.
            // Callers treat false as "already present" and do no further
            // writes in this transaction, so clearing is safe here.
            entities.clear();
            return false;
        }
    }

    @Override
    public void update(RewardClaim claim) {
        jpa.save(toEntity(claim));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardClaim> listByWallet(String walletAddress, int take) {
        return jpa.findByWalletAddressOrderByClaimedAtUtcDesc(walletAddress).stream().limit(take)
                .map(JpaRewardClaimRepositoryAdapter::toDomain).toList();
    }

    static RewardClaimEntity toEntity(RewardClaim claim) {
        RewardClaimEntity e = new RewardClaimEntity();
        e.id = claim.getId();
        e.walletAddress = claim.getWalletAddress();
        e.amount = claim.getAmount();
        e.claimType = claim.getClaimType();
        e.transactionHash = claim.getTransactionHash();
        e.paymentTransactionHash = claim.getPaymentTransactionHash();
        e.paymentAmount = claim.getPaymentAmount();
        e.paymentChainId = claim.getPaymentChainId();
        e.paymentNetworkName = claim.getPaymentNetworkName();
        e.paymentTokenContract = claim.getPaymentTokenContract();
        e.marketplaceWallet = claim.getMarketplaceWallet();
        e.allocationName = claim.getAllocationName();
        e.paymentExplorerUrl = claim.getPaymentExplorerUrl();
        e.mintExplorerUrl = claim.getMintExplorerUrl();
        e.claimedAtUtc = claim.getClaimedAtUtc();
        return e;
    }

    static RewardClaim toDomain(RewardClaimEntity e) {
        RewardClaim claim = new RewardClaim();
        claim.setId(e.id);
        claim.setWalletAddress(e.walletAddress);
        claim.setAmount(e.amount);
        claim.setClaimType(e.claimType);
        claim.setTransactionHash(e.transactionHash);
        claim.setPaymentTransactionHash(e.paymentTransactionHash);
        claim.setPaymentAmount(e.paymentAmount);
        claim.setPaymentChainId(e.paymentChainId);
        claim.setPaymentNetworkName(e.paymentNetworkName);
        claim.setPaymentTokenContract(e.paymentTokenContract);
        claim.setMarketplaceWallet(e.marketplaceWallet);
        claim.setAllocationName(e.allocationName);
        claim.setPaymentExplorerUrl(e.paymentExplorerUrl);
        claim.setMintExplorerUrl(e.mintExplorerUrl);
        claim.setClaimedAtUtc(e.claimedAtUtc);
        return claim;
    }
}

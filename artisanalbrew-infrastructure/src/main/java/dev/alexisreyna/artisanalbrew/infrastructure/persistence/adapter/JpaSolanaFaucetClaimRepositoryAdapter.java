package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.SolanaFaucetClaimRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.SolanaFaucetClaim;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.SolanaFaucetClaimEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.SolanaFaucetClaimJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** JPA adapter for {@link SolanaFaucetClaimRepository}. */
@Repository
@Transactional
public class JpaSolanaFaucetClaimRepositoryAdapter implements SolanaFaucetClaimRepository {
    private final SolanaFaucetClaimJpaRepository jpa;

    public JpaSolanaFaucetClaimRepositoryAdapter(SolanaFaucetClaimJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SolanaFaucetClaim> findLatest(String chainKey, String walletAddress) {
        return jpa.findFirstByChainKeyAndWalletAddressOrderByClaimedAtUtcDesc(chainKey, walletAddress)
                .map(JpaSolanaFaucetClaimRepositoryAdapter::toDomain);
    }

    @Override
    public void add(SolanaFaucetClaim claim) {
        SolanaFaucetClaimEntity e = new SolanaFaucetClaimEntity();
        e.id = claim.getId();
        e.chainKey = claim.getChainKey();
        e.walletAddress = claim.getWalletAddress();
        e.amount = claim.getAmount();
        e.rawAmount = claim.getRawAmount();
        e.signature = claim.getSignature();
        e.claimedAtUtc = claim.getClaimedAtUtc();
        jpa.save(e);
    }

    static SolanaFaucetClaim toDomain(SolanaFaucetClaimEntity e) {
        SolanaFaucetClaim claim = new SolanaFaucetClaim();
        claim.setId(e.id);
        claim.setChainKey(e.chainKey);
        claim.setWalletAddress(e.walletAddress);
        claim.setAmount(e.amount);
        claim.setRawAmount(e.rawAmount);
        claim.setSignature(e.signature);
        claim.setClaimedAtUtc(e.claimedAtUtc);
        return claim;
    }
}

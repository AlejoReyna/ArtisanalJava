package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;

import java.util.List;
import java.util.Optional;

/** Port of {@code Repositories/IRewardClaimRepository.cs}. */
public interface RewardClaimRepository {
    Optional<RewardClaim> getLatestDailyClaim(String walletAddress);

    void add(RewardClaim claim);

    boolean existsByPaymentHash(String paymentTransactionHash);

    /**
     * Adds a claim, returning false if the payment was already claimed —
     * a return value rather than a provider-specific exception.
     */
    boolean tryAdd(RewardClaim claim);

    void update(RewardClaim claim);

    List<RewardClaim> listByWallet(String walletAddress, int take);

    default List<RewardClaim> listByWallet(String walletAddress) {
        return listByWallet(walletAddress, 20);
    }
}

package dev.alexisreyna.artisanalbrew.application.service.rewards;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.RewardClaimModels;

/** Port of {@code Rewards/IRewardClaimService.cs}. */
public interface RewardClaimService {
    RewardClaimModels.Status getClaimStatus(String walletAddress);

    RewardClaimModels.Result claimDailyReward(String walletAddress);

    RewardClaimModels.Result mintLoyaltyReward(String walletAddress, java.math.BigDecimal amount, String paymentTransactionHash);
}

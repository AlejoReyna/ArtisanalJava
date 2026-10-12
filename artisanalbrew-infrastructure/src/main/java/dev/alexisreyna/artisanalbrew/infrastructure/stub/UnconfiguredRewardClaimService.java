package dev.alexisreyna.artisanalbrew.infrastructure.stub;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.RewardClaimModels;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;

import java.math.BigDecimal;

/** Fail-closed reward service: nothing claimable, minting disabled. */
public class UnconfiguredRewardClaimService implements RewardClaimService {

    @Override
    public RewardClaimModels.Status getClaimStatus(String walletAddress) {
        RewardClaimModels.Status status = new RewardClaimModels.Status();
        status.setWalletAddress(walletAddress);
        status.setMintingEnabled(false);
        return status;
    }

    @Override
    public RewardClaimModels.Result claimDailyReward(String walletAddress) {
        RewardClaimModels.Result result = new RewardClaimModels.Result();
        result.setSuccess(false);
        result.setMintedAmount(BigDecimal.ZERO);
        result.setError("Minting is not configured on the server.");
        return result;
    }

    @Override
    public RewardClaimModels.Result mintLoyaltyReward(String walletAddress, BigDecimal amount, String paymentTransactionHash) {
        RewardClaimModels.Result result = new RewardClaimModels.Result();
        result.setSuccess(false);
        result.setMintedAmount(BigDecimal.ZERO);
        result.setPaymentTransactionHash(paymentTransactionHash);
        result.setError("Minting is not configured on the server.");
        return result;
    }
}

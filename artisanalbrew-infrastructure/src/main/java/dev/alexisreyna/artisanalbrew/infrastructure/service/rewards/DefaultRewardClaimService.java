package dev.alexisreyna.artisanalbrew.infrastructure.service.rewards;

import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.RewardClaimModels;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardCalculator;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;
import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Port of {@code RewardClaimService} ({@code Infrastructure/Services/Rewards}).
 */
@Service
public class DefaultRewardClaimService implements RewardClaimService {
    private final CoffeeWeb3Service web3;
    private final RewardClaimRepository claims;

    public DefaultRewardClaimService(CoffeeWeb3Service web3, RewardClaimRepository claims) {
        this.web3 = web3;
        this.claims = claims;
    }

    @Override
    public RewardClaimModels.Status getClaimStatus(String walletAddress) {
        var dashboard = web3.getDashboardData(walletAddress);
        var lastClaim = claims.getLatestDailyClaim(walletAddress).orElse(null);
        BigDecimal dailyReward = RewardCalculator.dailyRewardFromPaymentToken(
                dashboard.getPaymentTokenBalance(), dashboard.getCurrentApr());
        boolean canClaimToday = canClaimToday(lastClaim == null ? null : lastClaim.getClaimedAtUtc());
        BigDecimal claimable = canClaimToday && dailyReward.signum() > 0 ? dailyReward : BigDecimal.ZERO;

        RewardClaimModels.Status status = new RewardClaimModels.Status();
        status.setWalletAddress(walletAddress);
        status.setPaymentTokenBalance(dashboard.getPaymentTokenBalance());
        status.setEstimatedDailyReward(dailyReward);
        status.setClaimableAmount(claimable);
        status.setCanClaimToday(canClaimToday && claimable.signum() > 0);
        status.setMintingEnabled(web3.isMintingConfigured());
        status.setLastClaimedAtUtc(lastClaim == null ? null : lastClaim.getClaimedAtUtc());
        return status;
    }

    @Override
    public RewardClaimModels.Result claimDailyReward(String walletAddress) {
        RewardClaimModels.Status status = getClaimStatus(walletAddress);
        if (!status.isMintingEnabled()) {
            return failure("Minting is not configured. Add CoffeeCoinOwner:PrivateKey to User Secrets.");
        }
        if (!status.isCanClaimToday() || status.getClaimableAmount().signum() <= 0) {
            return failure(status.getClaimableAmount().signum() <= 0
                    ? "No claimable rewards. Configure and fund the payment token balance first."
                    : "You already claimed today's reward. Come back tomorrow.");
        }
        return mintAndRecord(walletAddress, status.getClaimableAmount(), "daily", null);
    }

    @Override
    public RewardClaimModels.Result mintLoyaltyReward(String walletAddress, BigDecimal amount, String paymentTransactionHash) {
        if (amount == null || amount.signum() <= 0) {
            return failure("Loyalty reward amount must be greater than zero.");
        }
        if (!isTransactionHash(paymentTransactionHash)) {
            return failure("A valid payment token transaction hash is required.");
        }
        if (!web3.isMintingConfigured()) {
            return failure("Minting is not configured on the server.");
        }
        return mintAndRecord(walletAddress, amount, "loyalty", paymentTransactionHash);
    }

    private RewardClaimModels.Result mintAndRecord(String walletAddress, BigDecimal amount, String claimType,
                                                   String paymentTransactionHash) {
        try {
            String txHash = web3.mintCoffeeCoin(walletAddress, amount);
            RewardClaim claim = new RewardClaim();
            claim.setWalletAddress(walletAddress);
            claim.setAmount(amount);
            claim.setClaimType(claimType);
            claim.setTransactionHash(txHash);
            claim.setPaymentTransactionHash(paymentTransactionHash);
            claim.setClaimedAtUtc(Instant.now());
            claims.add(claim);

            RewardClaimModels.Result result = new RewardClaimModels.Result();
            result.setSuccess(true);
            result.setTransactionHash(txHash);
            result.setPaymentTransactionHash(paymentTransactionHash);
            result.setMintedAmount(amount);
            return result;
        } catch (RuntimeException e) {
            return failure(e.getMessage());
        }
    }

    private static RewardClaimModels.Result failure(String error) {
        RewardClaimModels.Result result = new RewardClaimModels.Result();
        result.setSuccess(false);
        result.setMintedAmount(BigDecimal.ZERO);
        result.setError(error);
        return result;
    }

    static boolean canClaimToday(Instant lastClaimedAtUtc) {
        if (lastClaimedAtUtc == null) {
            return true;
        }
        return lastClaimedAtUtc.atZone(ZoneOffset.UTC).toLocalDate()
                .isBefore(LocalDate.now(ZoneOffset.UTC));
    }

    private static boolean isTransactionHash(String value) {
        return value != null && value.length() == 66
                && (value.startsWith("0x") || value.startsWith("0X"))
                && value.substring(2).chars().allMatch(c ->
                        (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
    }
}

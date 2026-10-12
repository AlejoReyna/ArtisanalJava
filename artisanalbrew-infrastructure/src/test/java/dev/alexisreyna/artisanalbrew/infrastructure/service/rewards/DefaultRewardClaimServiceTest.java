package dev.alexisreyna.artisanalbrew.infrastructure.service.rewards;

import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeDashboardModel;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Daily/loyalty claim math over a stubbed chain service: claimable amounts,
 * once-per-day gating, and the unconfigured-minting branches.
 */
class DefaultRewardClaimServiceTest {

    static final class FakeClaims implements RewardClaimRepository {
        final List<RewardClaim> rows = new ArrayList<>();

        @Override
        public Optional<RewardClaim> getLatestDailyClaim(String walletAddress) {
            return rows.stream()
                    .filter(c -> c.getWalletAddress().equals(walletAddress) && "daily".equals(c.getClaimType()))
                    .max(java.util.Comparator.comparing(RewardClaim::getClaimedAtUtc));
        }

        @Override
        public void add(RewardClaim claim) {
            rows.add(claim);
        }

        @Override
        public boolean existsByPaymentHash(String hash) {
            return rows.stream().anyMatch(c -> hash.equals(c.getPaymentTransactionHash()));
        }

        @Override
        public boolean tryAdd(RewardClaim claim) {
            if (existsByPaymentHash(claim.getPaymentTransactionHash())) {
                return false;
            }
            rows.add(claim);
            return true;
        }

        @Override
        public void update(RewardClaim claim) {
        }

        @Override
        public List<RewardClaim> listByWallet(String walletAddress, int take) {
            return rows.stream().filter(c -> c.getWalletAddress().equals(walletAddress)).limit(take).toList();
        }
    }

    private static CoffeeWeb3Service web3(boolean minting, BigDecimal paymentBalance) {
        CoffeeWeb3Service web3 = mock(CoffeeWeb3Service.class);
        when(web3.isMintingConfigured()).thenReturn(minting);
        CoffeeDashboardModel dashboard = new CoffeeDashboardModel();
        dashboard.setPaymentTokenBalance(paymentBalance);
        dashboard.setCurrentApr(new BigDecimal("5.2"));
        when(web3.getDashboardData(any())).thenReturn(dashboard);
        when(web3.mintCoffeeCoin(any(), any())).thenReturn("0xmint");
        return web3;
    }

    @Test
    void dailyClaimFlow() {
        var service = new DefaultRewardClaimService(web3(true, new BigDecimal("100")), new FakeClaims());
        var status = service.getClaimStatus("0xwallet");
        assertThat(status.isCanClaimToday()).isTrue();
        assertThat(status.getClaimableAmount()).isPositive();
        assertThat(status.isMintingEnabled()).isTrue();

        var result = service.claimDailyReward("0xwallet");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionHash()).isEqualTo("0xmint");
        // Second claim the same day is refused.
        assertThat(service.claimDailyReward("0xwallet").isSuccess()).isFalse();

        var unconfigured = new DefaultRewardClaimService(web3(false, new BigDecimal("100")), new FakeClaims());
        assertThat(unconfigured.claimDailyReward("0xwallet").isSuccess()).isFalse();
        var empty = new DefaultRewardClaimService(web3(true, BigDecimal.ZERO), new FakeClaims());
        assertThat(empty.getClaimStatus("0xwallet").isCanClaimToday()).isFalse();
    }

    @Test
    void loyaltyValidatesInputs() {
        var service = new DefaultRewardClaimService(web3(true, BigDecimal.ZERO), new FakeClaims());
        assertThat(service.mintLoyaltyReward("0xwallet", BigDecimal.ZERO, "0x" + "ab".repeat(32)).isSuccess())
                .isFalse();
        assertThat(service.mintLoyaltyReward("0xwallet", BigDecimal.ONE, "nope").isSuccess()).isFalse();
        var ok = service.mintLoyaltyReward("0xwallet", new BigDecimal("3"), "0x" + "ab".repeat(32));
        assertThat(ok.isSuccess()).isTrue();
        assertThat(ok.getMintedAmount()).isEqualByComparingTo("3");
    }

    @Test
    void canClaimTodayBoundary() {
        assertThat(DefaultRewardClaimService.canClaimToday(null)).isTrue();
        assertThat(DefaultRewardClaimService.canClaimToday(Instant.now())).isFalse();
        assertThat(DefaultRewardClaimService.canClaimToday(
                Instant.now().minusSeconds(25 * 3600))).isTrue();
    }
}

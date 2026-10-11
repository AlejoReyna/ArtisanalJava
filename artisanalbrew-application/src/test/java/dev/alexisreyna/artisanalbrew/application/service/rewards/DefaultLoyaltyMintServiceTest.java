package dev.alexisreyna.artisanalbrew.application.service.rewards;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.repository.RewardClaimRepository;
import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.domain.entities.RewardClaim;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultLoyaltyMintServiceTest {

    static final class FakeClaims implements RewardClaimRepository {
        final List<RewardClaim> rows = new ArrayList<>();

        @Override
        public Optional<RewardClaim> getLatestDailyClaim(String walletAddress) {
            return Optional.empty();
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

    static final class FakeUow implements UnitOfWork {
        boolean committed;

        @Override
        public UnitOfWorkTransaction beginSerializableTransaction() {
            return new UnitOfWorkTransaction() {
                @Override
                public void commit() {
                    committed = true;
                }

                @Override
                public void close() {
                }
            };
        }

        @Override
        public UnitOfWorkTransaction beginTransaction() {
            return beginSerializableTransaction();
        }
    }

    static class FakeMintWeb3 implements CoffeeWeb3Service {
        TransactionVerificationStatus paymentStatus = TransactionVerificationStatus.VERIFIED;
        boolean mintingConfigured = true;
        boolean failMint;

        @Override
        public TransactionVerificationStatus verifyPaymentTransaction(String txHash, String expectedCustomer, BigDecimal expectedAmount) {
            return paymentStatus;
        }

        @Override
        public boolean isMintingConfigured() {
            return mintingConfigured;
        }

        @Override
        public String mintCoffeeCoin(String toAddress, BigDecimal amount) {
            if (failMint) {
                throw new RuntimeException("boom");
            }
            return "0xmint";
        }

        @Override
        public dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeDashboardModel getDashboardData(String w) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getCoffeeCoinBalance(String w) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getTotalCoffeeSupply() {
            throw new UnsupportedOperationException();
        }

        @Override
        public dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult verifyNativeEthPayment(String a, String b, BigDecimal c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getStakedPaymentTokenBalance(String w) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getPendingStakingRewards(String w) {
            throw new UnsupportedOperationException();
        }

        @Override
        public dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult verifyStakingTransaction(String a, String b, BigDecimal c, dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingTransactionType d) {
            throw new UnsupportedOperationException();
        }

        @Override
        public dev.alexisreyna.artisanalbrew.application.service.blockchain.CafeFaucetStatus getCafeFaucetStatus(String w) {
            throw new UnsupportedOperationException();
        }
    }

    private static LoyaltyMintCommand command(String paymentHash) {
        return new LoyaltyMintCommand("0xwallet", new BigDecimal("2.0"), new BigDecimal("2.0"), paymentHash, " allocation ");
    }

    @Test
    void mintFlowCoversAllBranches() {
        FakeClaims claims = new FakeClaims();
        FakeUow uow = new FakeUow();
        FakeMintWeb3 web3 = new FakeMintWeb3();
        var service = new DefaultLoyaltyMintService(web3, claims, uow);
        var chain = new BlockchainNetworkOptions();

        var ok = service.mint(chain, command("0xpay1"));
        assertThat(ok.status()).isEqualTo(LoyaltyMintStatus.MINTED);
        assertThat(ok.mintTransactionHash()).isEqualTo("0xmint");
        assertThat(uow.committed).isTrue();
        assertThat(claims.rows.get(0).getAllocationName()).isEqualTo("allocation");
        assertThat(claims.rows.get(0).getClaimType()).isEqualTo("allocation");
        assertThat(service.mint(chain, command("0xpay1")).status()).isEqualTo(LoyaltyMintStatus.ALREADY_CLAIMED);

        web3.paymentStatus = TransactionVerificationStatus.PENDING_CONFIRMATIONS;
        assertThat(service.mint(chain, command("0xpay2")).status())
                .isEqualTo(LoyaltyMintStatus.PENDING_CONFIRMATIONS);
        web3.paymentStatus = TransactionVerificationStatus.FAILED;
        assertThat(service.mint(chain, command("0xpay3")).status())
                .isEqualTo(LoyaltyMintStatus.PAYMENT_NOT_VERIFIED);

        web3.paymentStatus = TransactionVerificationStatus.VERIFIED;
        web3.mintingConfigured = false;
        assertThat(service.mint(chain, command("0xpay4")).status())
                .isEqualTo(LoyaltyMintStatus.MINTING_NOT_CONFIGURED);

        web3.mintingConfigured = true;
        web3.failMint = true;
        uow.committed = false;
        assertThat(service.mint(chain, command("0xpay5")).status()).isEqualTo(LoyaltyMintStatus.MINT_FAILED);
        // No commit → a real transaction rolls the claim row back.
        assertThat(uow.committed).isFalse();
    }
}

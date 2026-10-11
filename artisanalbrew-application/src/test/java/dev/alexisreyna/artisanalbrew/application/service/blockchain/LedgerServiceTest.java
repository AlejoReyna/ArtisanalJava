package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerRepository;
import dev.alexisreyna.artisanalbrew.application.repository.StakingLedgerWriteResult;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerServiceTest {

    static final class FakeLedger implements StakingLedgerRepository {
        final Map<String, StakingLedgerEntry> store = new HashMap<>();

        private static String key(String chain, String hash, int index) {
            return chain + "|" + hash + "|" + index;
        }

        @Override
        public boolean existsByTransactionHash(String transactionHash) {
            return store.values().stream().anyMatch(e -> e.getTransactionHash().equals(transactionHash));
        }

        @Override
        public Optional<StakingLedgerEntry> findByOperation(String chainKey, String transactionHash, int operationIndex) {
            return Optional.ofNullable(store.get(key(chainKey, transactionHash, operationIndex)));
        }

        @Override
        public StakingLedgerWriteResult addIfAbsent(StakingLedgerEntry entry) {
            String key = key(entry.getChainKey(), entry.getTransactionHash(), entry.getOperationIndex());
            StakingLedgerEntry existing = store.get(key);
            if (existing != null) {
                return StakingLedgerWriteResult.alreadyPresent(existing);
            }
            store.put(key, entry);
            return StakingLedgerWriteResult.written();
        }

        @Override
        public List<StakingLedgerEntry> listByWallet(String walletAddress, int take) {
            return store.values().stream().filter(e -> e.getWalletAddress().equals(walletAddress)).limit(take).toList();
        }
    }

    static final class FakeWeb3 implements CoffeeWeb3Service {
        StakingVerificationResult stakingResult =
                new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, new BigDecimal("5.0"), 3, 2);

        @Override
        public StakingVerificationResult verifyStakingTransaction(String txHash, String expectedWallet,
                                                                  BigDecimal expectedAmount, StakingTransactionType type) {
            return stakingResult;
        }

        @Override
        public CoffeeDashboardModel getDashboardData(String walletAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getCoffeeCoinBalance(String walletAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getTotalCoffeeSupply() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String mintCoffeeCoin(String toAddress, BigDecimal amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TransactionVerificationStatus verifyPaymentTransaction(String txHash, String expectedCustomer, BigDecimal expectedAmount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public StakingVerificationResult verifyNativeEthPayment(String txHash, String expectedWallet, BigDecimal expectedAmountEth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getStakedPaymentTokenBalance(String walletAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal getPendingStakingRewards(String walletAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CafeFaucetStatus getCafeFaucetStatus(String walletAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isMintingConfigured() {
            return true;
        }
    }

    static final class FakeGateway implements LiquidStakingGateway {
        LiquidTransactionVerificationResult result = new LiquidTransactionVerificationResult(true,
                TransactionVerificationStatus.VERIFIED, new BigDecimal("10"), new BigDecimal("9.9"),
                BigDecimal.ZERO, 12345L, 0, null);

        @Override
        public LiquidStakingDashboard getDashboard(String chainKey, String walletIdentifier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiquidTransactionVerificationResult verify(String chainKey, String walletIdentifier, String transactionId,
                                                          LiquidStakingOperation operation, BigDecimal expectedAmount) {
            return result;
        }
    }

    private static ChainDefinition chain() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEvmChainId(11155111);
        chain.setExplorerTransactionTemplate("https://sepolia.etherscan.io/tx/{0}");
        chain.getDeployment().setCafe("0xcafe");
        chain.getDeployment().setStCafe("0xstcafe");
        chain.getDeployment().setCoffee("0xcoffee");
        chain.getDeployment().setLiquidVault("0xvault");
        chain.getDeployment().setCafeDecimals(18);
        chain.getDeployment().setStCafeDecimals(18);
        chain.getDeployment().setCoffeeDecimals(18);
        return chain;
    }

    @Test
    void singleChainGuardsHashAndSplitsClaimColumns() {
        FakeLedger ledger = new FakeLedger();
        FakeWeb3 web3 = new FakeWeb3();
        var service = new DefaultStakingLedgerService(web3, ledger);
        var network = new dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions();
        network.setCoffeeCoinContract("0xcoffee");

        var recorded = service.record(network, "0xwallet", "0xhash", StakingTransactionType.STAKE, null);
        assertThat(recorded.status()).isEqualTo(StakingLedgerService.RecordStatus.RECORDED);
        assertThat(recorded.entry().getAssetAmount()).isEqualByComparingTo("5.0");
        assertThat(service.record(network, "0xwallet", "0xhash", StakingTransactionType.STAKE, null).status())
                .isEqualTo(StakingLedgerService.RecordStatus.ALREADY_RECORDED);

        var claim = service.record(network, "0xwallet", "0xhash2", StakingTransactionType.CLAIM, null);
        assertThat(claim.entry().getRewardAmount()).isEqualByComparingTo("5.0");
        assertThat(claim.entry().getRewardIdentifier()).isEqualTo("0xcoffee");

        web3.stakingResult = StakingVerificationResult.pending(1, 2);
        assertThat(service.record(network, "0xwallet", "0xhash3", StakingTransactionType.STAKE, null).status())
                .isEqualTo(StakingLedgerService.RecordStatus.PENDING_CONFIRMATIONS);
        assertThat(service.getRecentActivity(" ")).isEmpty();
    }

    @Test
    void liquidPathIsIdempotentAndComputesRawAmounts() {
        FakeLedger ledger = new FakeLedger();
        FakeGateway gateway = new FakeGateway();
        var service = new DefaultLiquidStakingLedgerService(gateway, ledger);
        ChainDefinition chain = chain();

        var first = service.record(chain, "0xwallet", "0xtx", LiquidStakingOperation.DEPOSIT, null);
        assertThat(first.status()).isEqualTo(LiquidStakingLedgerService.RecordStatus.RECORDED);
        assertThat(first.entry().getRawAssetAmount()).isEqualTo("10000000000000000000");
        assertThat(first.entry().getActionType()).isEqualTo("deposit");
        var repeat = service.record(chain, "0xwallet", "0xtx", LiquidStakingOperation.DEPOSIT, null);
        assertThat(repeat.status()).isEqualTo(LiquidStakingLedgerService.RecordStatus.ALREADY_RECORDED);

        gateway.result = new LiquidTransactionVerificationResult(false,
                TransactionVerificationStatus.PENDING_CONFIRMATIONS, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0, 0, null);
        assertThat(service.record(chain, "0xwallet", "0xtx2", LiquidStakingOperation.CLAIM, null).status())
                .isEqualTo(LiquidStakingLedgerService.RecordStatus.PENDING_CONFIRMATIONS);

        gateway.result = LiquidTransactionVerificationResult.failed("nope");
        assertThat(service.record(chain, "0xwallet", "0xtx3", LiquidStakingOperation.CLAIM, null).status())
                .isEqualTo(LiquidStakingLedgerService.RecordStatus.VERIFICATION_FAILED);
    }
}

package dev.alexisreyna.artisanalbrew.infrastructure.stub;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.CafeFaucetStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeDashboardModel;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingTransactionType;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Fail-closed EVM gateway: every read is empty, every verification fails —
 * exactly what the real gateway answers while its chain is unconfigured.
 * Backs off once a configured gateway bean exists.
 */
public class UnconfiguredCoffeeWeb3Service implements CoffeeWeb3Service {
    private static final Logger log = LoggerFactory.getLogger(UnconfiguredCoffeeWeb3Service.class);

    @Override
    public CoffeeDashboardModel getDashboardData(String walletAddress) {
        CoffeeDashboardModel model = new CoffeeDashboardModel();
        model.setWalletAddress(walletAddress);
        return model;
    }

    @Override
    public BigDecimal getCoffeeCoinBalance(String walletAddress) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalCoffeeSupply() {
        return BigDecimal.ZERO;
    }

    @Override
    public String mintCoffeeCoin(String toAddress, BigDecimal amount) {
        log.warn("Minting is not configured; refusing to mint {} to {}", amount, toAddress);
        throw new IllegalStateException("Minting is not configured on the server.");
    }

    @Override
    public TransactionVerificationStatus verifyPaymentTransaction(String txHash, String expectedCustomer, BigDecimal expectedAmount) {
        return TransactionVerificationStatus.FAILED;
    }

    @Override
    public StakingVerificationResult verifyNativeEthPayment(String txHash, String expectedWallet, BigDecimal expectedAmountEth) {
        return StakingVerificationResult.FAILED;
    }

    @Override
    public BigDecimal getStakedPaymentTokenBalance(String walletAddress) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getPendingStakingRewards(String walletAddress) {
        return null;
    }

    @Override
    public StakingVerificationResult verifyStakingTransaction(String txHash, String expectedWallet,
                                                              BigDecimal expectedAmount, StakingTransactionType transactionType) {
        return StakingVerificationResult.FAILED;
    }

    @Override
    public CafeFaucetStatus getCafeFaucetStatus(String walletAddress) {
        return new CafeFaucetStatus();
    }

    @Override
    public boolean isMintingConfigured() {
        return false;
    }
}

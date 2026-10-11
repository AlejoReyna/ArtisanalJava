package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code ICoffeeWeb3Service} in {@code Blockchain/ICoffeeWeb3Service.cs}. */
public interface CoffeeWeb3Service {
    CoffeeDashboardModel getDashboardData(String walletAddress);

    BigDecimal getCoffeeCoinBalance(String walletAddress);

    BigDecimal getTotalCoffeeSupply();

    String mintCoffeeCoin(String toAddress, BigDecimal amount);

    TransactionVerificationStatus verifyPaymentTransaction(String txHash, String expectedCustomer, BigDecimal expectedAmount);

    StakingVerificationResult verifyNativeEthPayment(String txHash, String expectedWallet, BigDecimal expectedAmountEth);

    BigDecimal getStakedPaymentTokenBalance(String walletAddress);

    /** Null on RPC failure rather than a false zero. */
    BigDecimal getPendingStakingRewards(String walletAddress);

    StakingVerificationResult verifyStakingTransaction(String txHash, String expectedWallet,
                                                       BigDecimal expectedAmount, StakingTransactionType transactionType);

    CafeFaucetStatus getCafeFaucetStatus(String walletAddress);

    boolean isMintingConfigured();
}

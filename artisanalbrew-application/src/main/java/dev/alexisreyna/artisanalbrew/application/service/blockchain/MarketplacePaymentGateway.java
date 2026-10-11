package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigDecimal;

/** Port of {@code IMarketplacePaymentGateway} in {@code Blockchain/IMarketplacePaymentGateway.cs}. */
public interface MarketplacePaymentGateway {
    StakingVerificationResult verifyNativePayment(String chainKey, String transactionHash,
                                                  String expectedFromWallet, BigDecimal expectedAmount);
}

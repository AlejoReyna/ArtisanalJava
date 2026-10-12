package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.ConfirmationCalculator;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.MarketplacePaymentGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * web3j port of {@code EvmMarketplacePaymentGateway}. Note the deliberate
 * asymmetry with the legacy checkout path: a missing receipt means
 * not-yet-mined ({@code Pending}), never {@code Failed}.
 */
public class Web3jMarketplacePaymentGateway implements MarketplacePaymentGateway {
    private static final Logger log = LoggerFactory.getLogger(Web3jMarketplacePaymentGateway.class);

    private final ChainRegistry registry;

    public Web3jMarketplacePaymentGateway(ChainRegistry registry) {
        this.registry = registry;
    }

    @Override
    public StakingVerificationResult verifyNativePayment(String chainKey, String transactionHash,
                                                         String expectedFromWallet, BigDecimal expectedAmount) {
        Optional<ChainDefinition> chain = registry.tryGet(chainKey);
        if (chain.isEmpty() || !chain.get().isEnabled() || chain.get().getFamily() != ChainFamily.EVM
                || !chain.get().getCapabilities().marketplacePayment()
                || chain.get().getDeployment().getLegacyPool().isBlank()) {
            return StakingVerificationResult.FAILED;
        }
        ChainDefinition definition = chain.get();
        if (!isTransactionHash(transactionHash) || !isValidAddress(expectedFromWallet)
                || expectedAmount == null || expectedAmount.signum() <= 0) {
            return StakingVerificationResult.FAILED;
        }
        try (EvmRpcClient rpc = new EvmRpcClient(definition.effectiveServerRpcUrl())) {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(transactionHash);
            if (receipt.isEmpty()) {
                // Not yet mined — the expected state on the first poll or two.
                return StakingVerificationResult.pending(0, definition.getMinimumConfirmations());
            }
            if (!EvmRpcClient.isSuccessful(receipt.get())
                    || !matches(receipt.get().getTo(), definition.getDeployment().getLegacyPool())) {
                return StakingVerificationResult.FAILED;
            }
            int confirmations = confirmations(rpc, receipt.get());
            if (confirmations < definition.getMinimumConfirmations()) {
                return StakingVerificationResult.pending(confirmations, definition.getMinimumConfirmations());
            }
            var transaction = rpc.getTransaction(transactionHash);
            BigInteger expectedWei = Convert.toWei(expectedAmount, Convert.Unit.ETHER).toBigInteger();
            if (transaction.isEmpty() || !matches(transaction.get().getFrom(), expectedFromWallet)
                    || !matches(transaction.get().getTo(), definition.getDeployment().getLegacyPool())
                    || !expectedWei.equals(transaction.get().getValue())) {
                return StakingVerificationResult.FAILED;
            }
            return new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, expectedAmount,
                    confirmations, definition.getMinimumConfirmations());
        } catch (Exception e) {
            log.warn("Marketplace payment verification failed for chain {} and transaction {}", chainKey, transactionHash, e);
            return StakingVerificationResult.FAILED;
        }
    }

    private static int confirmations(EvmRpcClient rpc, TransactionReceipt receipt) throws java.io.IOException {
        if (receipt.getBlockNumber() == null) {
            return 0;
        }
        Optional<BigInteger> current = rpc.getBlockNumber();
        if (current.isEmpty()) {
            return 0;
        }
        return ConfirmationCalculator.calculate(current.get(), receipt.getBlockNumber());
    }

    static boolean matches(String actual, String expected) {
        return actual != null && !actual.isBlank() && actual.equalsIgnoreCase(expected);
    }

    static boolean isValidAddress(String address) {
        return WalletAddressRules.isValidEthereumAddress(address);
    }

    static boolean isTransactionHash(String value) {
        return WalletAddressRules.tryNormalizeTransactionHash(value).isPresent();
    }
}

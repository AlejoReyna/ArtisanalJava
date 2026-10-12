package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.ConfirmationCalculator;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingDashboard;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingOperation;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidTransactionVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * web3j port of {@code EvmLiquidStakingGateway} (ERC-4626 vault reads and
 * deposit/redeem/claim verification). This gateway only serves EVM chains;
 * Solana has its own.
 */
public class Web3jLiquidStakingGateway implements LiquidStakingGateway {
    private static final Logger log = LoggerFactory.getLogger(Web3jLiquidStakingGateway.class);
    private static final BigInteger ONE_TOKEN = BigInteger.TEN.pow(18);

    private final ChainRegistry registry;

    public Web3jLiquidStakingGateway(ChainRegistry registry) {
        this.registry = registry;
    }

    @Override
    public LiquidStakingDashboard getDashboard(String chainKey, String walletIdentifier) {
        Optional<ChainDefinition> chain = registry.tryGet(chainKey).filter(ChainDefinition::isEnabled);
        if (chain.isEmpty() || chain.get().getFamily() != ChainFamily.EVM) {
            return unavailable(chainKey, chain.map(ChainDefinition::getKey).orElse(chainKey),
                    "This gateway only serves EVM chains.", walletIdentifier);
        }
        ChainDefinition definition = chain.get();
        if (!definition.getCapabilities().liquidStaking()
                || definition.getDeployment().getLiquidVault().isBlank()) {
            return unavailable(chainKey, definition.getKey(),
                    "Liquid staking is not deployed on this chain yet.", walletIdentifier);
        }
        if (!isAddress(walletIdentifier) || !isAddress(definition.getDeployment().getCafe())
                || !isAddress(definition.getDeployment().getCoffee())
                || !isAddress(definition.getDeployment().getStCafe())) {
            return unavailable(chainKey, definition.getKey(),
                    "The selected chain has an incomplete liquid-staking manifest.", walletIdentifier);
        }
        try (EvmRpcClient rpc = new EvmRpcClient(definition.effectiveServerRpcUrl())) {
            String vault = definition.getDeployment().getLiquidVault();
            BigInteger cafeBalance = balanceOf(rpc, definition.getDeployment().getCafe(), walletIdentifier);
            BigInteger shareBalance = balanceOf(rpc, definition.getDeployment().getStCafe(), walletIdentifier);
            BigInteger coffeeBalance = balanceOf(rpc, definition.getDeployment().getCoffee(), walletIdentifier);
            BigInteger redeemable = rpc.callUint256(vault,
                    EvmRpcClient.encodeCall("previewRedeem", new Uint256(shareBalance))).orElse(BigInteger.ZERO);
            BigInteger preview = rpc.callUint256(vault,
                    EvmRpcClient.encodeCall("previewDeposit", new Uint256(ONE_TOKEN))).orElse(BigInteger.ZERO);
            BigInteger earned = rpc.callUint256(vault,
                    EvmRpcClient.encodeCall("earned", new Address(walletIdentifier))).orElse(BigInteger.ZERO);
            BigInteger nativeBalance = rpc.getBalance(walletIdentifier).orElse(BigInteger.ZERO);
            BigInteger assets = rpc.callUint256(vault, Web3jCoffeeService.encodeNoArgs("totalAssets")).orElse(BigInteger.ZERO);
            BigInteger supply = rpc.callUint256(vault, Web3jCoffeeService.totalSupplyCall()).orElse(BigInteger.ZERO);

            LiquidStakingDashboard dashboard = new LiquidStakingDashboard();
            dashboard.setChainKey(definition.getKey());
            dashboard.setFamily(definition.familyName());
            dashboard.setWalletIdentifier(walletIdentifier);
            dashboard.setConfigured(true);
            dashboard.setCafeBalance(Web3jCoffeeService.fromWei(cafeBalance));
            dashboard.setStCafeBalance(Web3jCoffeeService.fromWei(shareBalance));
            dashboard.setRedeemableCafe(Web3jCoffeeService.fromWei(redeemable));
            dashboard.setExchangeRate(supply.equals(BigInteger.ZERO) ? BigDecimal.ONE
                    : Web3jCoffeeService.fromWei(assets).divide(Web3jCoffeeService.fromWei(supply), MathContext.DECIMAL128));
            dashboard.setPendingCoffee(Web3jCoffeeService.fromWei(earned));
            dashboard.setCoffeeBalance(Web3jCoffeeService.fromWei(coffeeBalance));
            dashboard.setDepositPreviewShares(Web3jCoffeeService.fromWei(preview));
            dashboard.setNativeGasBalance(Web3jCoffeeService.fromWei(nativeBalance));
            dashboard.setVaultIdentifier(vault);
            dashboard.setAssetIdentifier(definition.getDeployment().getCafe());
            dashboard.setReceiptIdentifier(definition.getDeployment().getStCafe());
            dashboard.setRewardIdentifier(definition.getDeployment().getCoffee());
            dashboard.setReadAtUtc(OffsetDateTime.now());
            return dashboard;
        } catch (Exception e) {
            log.warn("Liquid dashboard read failed for chain {}", chainKey, e);
            return unavailable(chainKey, chainKey, "The selected chain RPC is unavailable. Try again shortly.",
                    walletIdentifier);
        }
    }

    @Override
    public LiquidTransactionVerificationResult verify(String chainKey, String walletIdentifier, String transactionId,
                                                      LiquidStakingOperation operation, BigDecimal expectedAmount) {
        Optional<ChainDefinition> chain = registry.tryGet(chainKey);
        if (chain.isEmpty() || chain.get().getFamily() != ChainFamily.EVM
                || !chain.get().getCapabilities().liquidStaking()
                || chain.get().getDeployment().getLiquidVault().isBlank()) {
            return LiquidTransactionVerificationResult.failed("Liquid staking is not configured on the selected chain.");
        }
        ChainDefinition definition = chain.get();
        if (!isAddress(walletIdentifier) || !isTransactionHash(transactionId)) {
            return LiquidTransactionVerificationResult.failed("Invalid wallet or transaction identifier.");
        }
        try (EvmRpcClient rpc = new EvmRpcClient(definition.effectiveServerRpcUrl())) {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(transactionId);
            var transaction = rpc.getTransaction(transactionId);
            if (receipt.isEmpty() || transaction.isEmpty() || !EvmRpcClient.isSuccessful(receipt.get())
                    || !matches(receipt.get().getTo(), definition.getDeployment().getLiquidVault())
                    || !matches(transaction.get().getFrom(), walletIdentifier)) {
                return LiquidTransactionVerificationResult.failed(
                        "Receipt, signer, or vault target did not match the trusted registry.");
            }
            String selector = switch (operation) {
                case DEPOSIT -> StakingCalldataDecoder.functionSelector("deposit(uint256,address)");
                case REDEEM -> StakingCalldataDecoder.functionSelector("redeem(uint256,address,address)");
                case CLAIM -> StakingCalldataDecoder.functionSelector("claimRewards()");
                default -> "";
            };
            String input = transaction.get().getInput();
            if (input == null || !input.regionMatches(true, 0, selector, 0, selector.length())) {
                return LiquidTransactionVerificationResult.failed("Transaction function did not match the requested operation.");
            }
            int confirmations = confirmations(rpc, receipt.get());
            if (confirmations < definition.getMinimumConfirmations()) {
                return new LiquidTransactionVerificationResult(false,
                        TransactionVerificationStatus.PENDING_CONFIRMATIONS,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        blockNumber(receipt.get()), 0, "Waiting for configured confirmations.");
            }
            List<Log> logs = receipt.get().getLogs() == null ? List.of() : receipt.get().getLogs();
            if (operation == LiquidStakingOperation.DEPOSIT) {
                Optional<BigInteger> assets = StakingCalldataDecoder.tryDecodeUintAt(input, 0);
                Optional<String> receiver = StakingCalldataDecoder.tryDecodeAddressAt(input, 1);
                if (assets.isEmpty() || receiver.isEmpty() || !matches(receiver.get(), walletIdentifier)) {
                    return LiquidTransactionVerificationResult.failed("Deposit calldata did not match the authenticated receiver.");
                }
                if (expectedAmount != null
                        && !assets.get().equals(Convert.toWei(expectedAmount, Convert.Unit.ETHER).toBigInteger())) {
                    return LiquidTransactionVerificationResult.failed("Deposit amount did not match the requested amount.");
                }
                var eventLog = logs.stream().flatMap(entry -> EvmLogEvents.deposit(entry).stream())
                        .filter(d -> matches(d.contract(), definition.getDeployment().getLiquidVault())
                                && matches(d.receiver(), walletIdentifier)
                                && d.assets().equals(assets.get()))
                        .findFirst();
                if (eventLog.isEmpty()) {
                    return LiquidTransactionVerificationResult.failed("The vault deposit event was not observed.");
                }
                var deposit = eventLog.get();
                return new LiquidTransactionVerificationResult(true, TransactionVerificationStatus.VERIFIED,
                        Web3jCoffeeService.fromWei(deposit.assets()), Web3jCoffeeService.fromWei(deposit.shares()),
                        BigDecimal.ZERO, blockNumber(receipt.get()), deposit.logIndex(), null);
            }
            if (operation == LiquidStakingOperation.REDEEM) {
                Optional<BigInteger> shares = StakingCalldataDecoder.tryDecodeUintAt(input, 0);
                Optional<String> receiver = StakingCalldataDecoder.tryDecodeAddressAt(input, 1);
                Optional<String> owner = StakingCalldataDecoder.tryDecodeAddressAt(input, 2);
                if (shares.isEmpty() || receiver.isEmpty() || owner.isEmpty()
                        || !matches(receiver.get(), walletIdentifier) || !matches(owner.get(), walletIdentifier)) {
                    return LiquidTransactionVerificationResult.failed(
                            "Redeem calldata did not match the authenticated owner and receiver.");
                }
                if (expectedAmount != null
                        && !shares.get().equals(Convert.toWei(expectedAmount, Convert.Unit.ETHER).toBigInteger())) {
                    return LiquidTransactionVerificationResult.failed("Redeem share amount did not match the requested amount.");
                }
                var eventLog = logs.stream().flatMap(entry -> EvmLogEvents.withdraw(entry).stream())
                        .filter(w -> matches(w.contract(), definition.getDeployment().getLiquidVault())
                                && matches(w.receiver(), walletIdentifier)
                                && w.shares().equals(shares.get()))
                        .findFirst();
                if (eventLog.isEmpty()) {
                    return LiquidTransactionVerificationResult.failed("The vault withdrawal event was not observed.");
                }
                var withdrawal = eventLog.get();
                return new LiquidTransactionVerificationResult(true, TransactionVerificationStatus.VERIFIED,
                        Web3jCoffeeService.fromWei(withdrawal.assets()), Web3jCoffeeService.fromWei(withdrawal.shares()),
                        BigDecimal.ZERO, blockNumber(receipt.get()), withdrawal.logIndex(), null);
            }
            var reward = logs.stream().flatMap(entry -> EvmLogEvents.transfer(entry).stream())
                    .filter(t -> matches(t.contract(), definition.getDeployment().getCoffee())
                            && matches(t.from(), definition.getDeployment().getLiquidVault())
                            && matches(t.to(), walletIdentifier))
                    .findFirst();
            if (reward.isEmpty()) {
                return LiquidTransactionVerificationResult.failed("No COFFEE reward transfer was observed.");
            }
            return new LiquidTransactionVerificationResult(true, TransactionVerificationStatus.VERIFIED,
                    BigDecimal.ZERO, BigDecimal.ZERO, Web3jCoffeeService.fromWei(reward.get().value()),
                    blockNumber(receipt.get()), reward.get().logIndex(), null);
        } catch (Exception e) {
            log.warn("Liquid transaction verification failed for chain {} and transaction {}", chainKey, transactionId, e);
            return LiquidTransactionVerificationResult.failed("The transaction could not be verified.");
        }
    }

    private static LiquidStakingDashboard unavailable(String chainKey, String key, String reason, String wallet) {
        LiquidStakingDashboard dashboard = new LiquidStakingDashboard();
        dashboard.setChainKey(key);
        dashboard.setFamily("Evm");
        dashboard.setWalletIdentifier(wallet);
        dashboard.setConfigured(false);
        dashboard.setUnavailableReason(reason);
        dashboard.setReadAtUtc(OffsetDateTime.now());
        return dashboard;
    }

    private static BigInteger balanceOf(EvmRpcClient rpc, String token, String wallet) throws IOException {
        return rpc.callUint256(token, EvmRpcClient.encodeCall("balanceOf", new Address(wallet)))
                .orElse(BigInteger.ZERO);
    }

    private static int confirmations(EvmRpcClient rpc, TransactionReceipt receipt) throws IOException {
        if (receipt.getBlockNumber() == null) {
            return 0;
        }
        Optional<BigInteger> current = rpc.getBlockNumber();
        if (current.isEmpty()) {
            return 0;
        }
        return ConfirmationCalculator.calculate(current.get(), receipt.getBlockNumber());
    }

    private static long blockNumber(TransactionReceipt receipt) {
        if (receipt.getBlockNumber() == null) {
            return 0;
        }
        return receipt.getBlockNumber().longValue();
    }

    static boolean isAddress(String value) {
        return WalletAddressRules.isValidEthereumAddress(value);
    }

    static boolean isTransactionHash(String value) {
        return WalletAddressRules.tryNormalizeTransactionHash(value).isPresent();
    }

    static boolean matches(String actual, String expected) {
        return actual != null && !actual.isBlank() && actual.equalsIgnoreCase(expected);
    }
}

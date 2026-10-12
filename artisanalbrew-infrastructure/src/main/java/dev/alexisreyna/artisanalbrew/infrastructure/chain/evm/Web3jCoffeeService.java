package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.CoffeeCoinOwnerOptions;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CafeFaucetStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeDashboardModel;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.ConfirmationCalculator;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingTransactionType;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * web3j port of {@code CoffeeWeb3Service} (single-chain legacy staking,
 * payments, faucet, minting). Verification order is preserved exactly:
 * receipt → confirmations → transaction → calldata → events.
 */
public class Web3jCoffeeService implements CoffeeWeb3Service {
    private static final Logger log = LoggerFactory.getLogger(Web3jCoffeeService.class);
    private static final long SECONDS_PER_YEAR = 365L * 24 * 60 * 60;

    private final EvmRpcClient rpc;
    private final BlockchainNetworkOptions chain;
    private final CoffeeCoinOwnerOptions owner;
    private final String paymentTokenContract;
    private final String coffeeCoinContract;
    private final String stakingPoolContract;
    private final String cafeFaucetContract;

    public Web3jCoffeeService(BlockchainNetworkOptions chain, CoffeeCoinOwnerOptions owner) {
        this(new EvmRpcClient(chain.getRpcUrl()), chain, owner);
    }

    Web3jCoffeeService(EvmRpcClient rpc, BlockchainNetworkOptions chain, CoffeeCoinOwnerOptions owner) {
        this.rpc = rpc;
        this.chain = chain;
        this.owner = owner;
        this.paymentTokenContract = chain.effectivePaymentTokenContract();
        this.coffeeCoinContract = chain.getCoffeeCoinContract();
        this.stakingPoolContract = chain.getStakingPoolContract();
        this.cafeFaucetContract = chain.getCafeFaucetContract();
    }

    @Override
    public boolean isMintingConfigured() {
        return owner.isConfigured();
    }

    @Override
    public BigDecimal getCoffeeCoinBalance(String walletAddress) {
        return erc20Balance(walletAddress, coffeeCoinContract);
    }

    @Override
    public BigDecimal getTotalCoffeeSupply() {
        if (!WalletAddressRules.isConfiguredAddress(coffeeCoinContract)) {
            return BigDecimal.ZERO;
        }
        try {
            Optional<BigInteger> supply =
                    rpc.callUint256(coffeeCoinContract, totalSupplyCall());
            return supply.map(Web3jCoffeeService::fromWei).orElse(BigDecimal.ZERO);
        } catch (IOException e) {
            log.warn("totalSupply read failed", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public CoffeeDashboardModel getDashboardData(String walletAddress) {
        CoffeeDashboardModel model = new CoffeeDashboardModel();
        if (walletAddress == null || walletAddress.isBlank()) {
            return model;
        }
        CompletableFuture<BigInteger> nativeFuture =
                CompletableFuture.supplyAsync(() -> unchecked(() -> rpc.getBalance(walletAddress).orElse(BigInteger.ZERO)));
        CompletableFuture<BigDecimal> paymentFuture =
                CompletableFuture.supplyAsync(() -> erc20Balance(walletAddress, paymentTokenContract));
        CompletableFuture<BigDecimal> stakedFuture =
                CompletableFuture.supplyAsync(() -> getStakedPaymentTokenBalance(walletAddress));
        CompletableFuture<BigDecimal> pendingFuture =
                CompletableFuture.supplyAsync(() -> getPendingStakingRewards(walletAddress));
        CompletableFuture<BigDecimal> coffeeFuture =
                CompletableFuture.supplyAsync(() -> getCoffeeCoinBalance(walletAddress));
        CompletableFuture<String> claimFuture =
                CompletableFuture.supplyAsync(() -> detectClaimFunction(walletAddress));
        CompletableFuture<BigDecimal> aprFuture = CompletableFuture.supplyAsync(this::detectContractApr);
        CompletableFuture.allOf(nativeFuture, paymentFuture, stakedFuture, pendingFuture,
                coffeeFuture, claimFuture, aprFuture).join();

        model.setWalletAddress(walletAddress);
        model.setNativeBalance(fromWei(nativeFuture.join()));
        model.setPaymentTokenBalance(paymentFuture.join());
        model.setStakedPaymentTokenBalance(stakedFuture.join());
        model.setPendingStakingRewards(pendingFuture.join());
        model.setCoffeeCoinBalance(coffeeFuture.join());
        BigDecimal contractApr = aprFuture.join();
        model.setCurrentApr(contractApr != null ? contractApr : chain.getStakingAprPercent());
        model.setAprIsContractDerived(contractApr != null);
        model.setClaimFunctionName(claimFuture.join());
        return model;
    }

    @Override
    public String mintCoffeeCoin(String toAddress, BigDecimal amount) {
        if (!owner.isConfigured()) {
            throw new IllegalStateException("CoffeeCoin owner private key is not configured. "
                    + "Set CoffeeCoinOwner:PrivateKey via User Secrets or environment variable CoffeeCoinOwner__PrivateKey.");
        }
        if (!WalletAddressRules.isConfiguredAddress(coffeeCoinContract)) {
            throw new IllegalStateException("Blockchain:Network:CoffeeCoinContract is not configured.");
        }
        if (!WalletAddressRules.isValidEthereumAddress(toAddress)) {
            throw new IllegalArgumentException("A valid recipient wallet address is required.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Mint amount must be greater than zero.");
        }
        try {
            String key = owner.getPrivateKey().trim();
            if (key.startsWith("0x") || key.startsWith("0X")) {
                key = key.substring(2);
            }
            Credentials credentials = Credentials.create(key);
            String from = credentials.getAddress();
            String data = FunctionEncoder.encode(new Function("mint",
                    List.of(new Address(toAddress), new Uint256(Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger())),
                    List.of()));
            BigInteger gasEstimate = rpc.estimateGas(from, coffeeCoinContract, data);
            BigInteger nonce = rpc.getTransactionCount(from);
            BigInteger gasPrice = rpc.getGasPrice();
            RawTransaction raw = RawTransaction.createTransaction(
                    nonce, gasPrice, gasEstimate.add(BigInteger.valueOf(50_000)),
                    coffeeCoinContract, BigInteger.ZERO, data);
            byte[] signed = TransactionEncoder.signMessage(raw, chain.getChainId(), credentials);
            String hash = rpc.sendRawTransaction(Numeric.toHexString(signed));
            TransactionReceipt receipt = waitForReceipt(hash);
            if (receipt == null || !EvmRpcClient.isSuccessful(receipt)) {
                throw new IllegalStateException("CoffeeCoin mint transaction failed. Hash: " + hash);
            }
            return receipt.getTransactionHash() != null ? receipt.getTransactionHash() : hash;
        } catch (IOException | RuntimeException e) {
            if (isGasFundingFailure(e)) {
                throw new IllegalStateException("CoffeeCoin mint failed because the configured owner wallet does not have enough "
                        + chain.getCurrencySymbol() + " on " + chain.getNetworkName() + " to pay gas.", e);
            }
            throw new IllegalStateException("CoffeeCoin mint failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TransactionVerificationStatus verifyPaymentTransaction(String txHash, String expectedCustomer, BigDecimal expectedAmount) {
        if (!isTransactionHash(txHash) || !isValidAddress(expectedCustomer)
                || expectedAmount == null || expectedAmount.signum() <= 0
                || !WalletAddressRules.isConfiguredAddress(paymentTokenContract)
                || !WalletAddressRules.isConfiguredAddress(chain.getMarketplaceWallet())) {
            return TransactionVerificationStatus.FAILED;
        }
        try {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(txHash);
            if (receipt.isEmpty() || !EvmRpcClient.isSuccessful(receipt.get())
                    || !matches(receipt.get().getTo(), paymentTokenContract)) {
                return TransactionVerificationStatus.FAILED;
            }
            int confirmations = confirmations(receipt.get());
            if (confirmations < chain.getMinimumConfirmations()) {
                return TransactionVerificationStatus.PENDING_CONFIRMATIONS;
            }
            var transaction = rpc.getTransaction(txHash);
            if (transaction.isEmpty() || !matches(transaction.get().getFrom(), expectedCustomer)
                    || !matches(transaction.get().getTo(), paymentTokenContract)) {
                return TransactionVerificationStatus.FAILED;
            }
            BigInteger expectedWei = Convert.toWei(expectedAmount, Convert.Unit.ETHER).toBigInteger();
            var transfer = StakingCalldataDecoder.tryDecodeErc20Transfer(transaction.get().getInput());
            if (transfer.isEmpty() || !matches(transfer.get().recipient(), chain.getMarketplaceWallet())
                    || !transfer.get().amount().equals(expectedWei)) {
                return TransactionVerificationStatus.FAILED;
            }
            boolean verified = logs(receipt.get()).stream()
                    .flatMap(entry -> EvmLogEvents.transfer(entry).stream())
                    .anyMatch(t -> matches(t.contract(), paymentTokenContract)
                            && matches(t.from(), expectedCustomer)
                            && matches(t.to(), chain.getMarketplaceWallet())
                            && t.value().equals(expectedWei));
            return verified ? TransactionVerificationStatus.VERIFIED : TransactionVerificationStatus.FAILED;
        } catch (IOException e) {
            log.warn("Payment verification failed for {}", txHash, e);
            return TransactionVerificationStatus.FAILED;
        }
    }

    @Override
    public StakingVerificationResult verifyNativeEthPayment(String txHash, String expectedWallet, BigDecimal expectedAmountEth) {
        if (!isTransactionHash(txHash) || !isValidAddress(expectedWallet)
                || expectedAmountEth == null || expectedAmountEth.signum() <= 0
                || !WalletAddressRules.isConfiguredAddress(chain.getMarketplaceWallet())) {
            return StakingVerificationResult.FAILED;
        }
        try {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(txHash);
            if (receipt.isEmpty() || !EvmRpcClient.isSuccessful(receipt.get())
                    || !matches(receipt.get().getTo(), chain.getMarketplaceWallet())) {
                return StakingVerificationResult.FAILED;
            }
            int confirmations = confirmations(receipt.get());
            if (confirmations < chain.getMinimumConfirmations()) {
                return StakingVerificationResult.pending(confirmations, chain.getMinimumConfirmations());
            }
            var transaction = rpc.getTransaction(txHash);
            BigInteger expectedWei = Convert.toWei(expectedAmountEth, Convert.Unit.ETHER).toBigInteger();
            if (transaction.isEmpty() || !matches(transaction.get().getFrom(), expectedWallet)
                    || !matches(transaction.get().getTo(), chain.getMarketplaceWallet())
                    || !expectedWei.equals(transaction.get().getValue())) {
                return StakingVerificationResult.FAILED;
            }
            return new StakingVerificationResult(TransactionVerificationStatus.VERIFIED, expectedAmountEth,
                    confirmations, chain.getMinimumConfirmations());
        } catch (IOException e) {
            log.warn("Native payment verification failed for {}", txHash, e);
            return StakingVerificationResult.FAILED;
        }
    }

    @Override
    public BigDecimal getStakedPaymentTokenBalance(String walletAddress) {
        if (!isValidAddress(walletAddress) || !WalletAddressRules.isConfiguredAddress(stakingPoolContract)) {
            return BigDecimal.ZERO;
        }
        try {
            Optional<BigInteger> balance = rpc.callUint256(stakingPoolContract,
                    EvmRpcClient.encodeCall("balanceOf", new Address(walletAddress)));
            return balance.map(Web3jCoffeeService::fromWei).orElse(BigDecimal.ZERO);
        } catch (IOException e) {
            log.warn("Staked balance read failed", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public BigDecimal getPendingStakingRewards(String walletAddress) {
        if (!isValidAddress(walletAddress) || !WalletAddressRules.isConfiguredAddress(stakingPoolContract)) {
            return BigDecimal.ZERO;
        }
        try {
            Optional<BigInteger> rewards = rpc.callUint256(stakingPoolContract,
                    EvmRpcClient.encodeCall("earned", new Address(walletAddress)));
            return rewards.map(Web3jCoffeeService::fromWei).orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            // Null on RPC failure rather than a false zero — same contract as .NET.
            log.warn("Failed to read pending staking rewards for {} from {}", walletAddress, stakingPoolContract, e);
            return null;
        }
    }

    @Override
    public CafeFaucetStatus getCafeFaucetStatus(String walletAddress) {
        CafeFaucetStatus status = new CafeFaucetStatus();
        if (!WalletAddressRules.isConfiguredAddress(cafeFaucetContract)
                || !WalletAddressRules.isConfiguredAddress(paymentTokenContract)) {
            status.setConfigured(false);
            return status;
        }
        status.setConfigured(true);
        if (!isValidAddress(walletAddress)) {
            return status;
        }
        try {
            Optional<BigInteger> claimAmount = rpc.callUint256(cafeFaucetContract, encodeNoArgs("claimAmount"));
            Optional<BigInteger> cooldown = rpc.callUint256(cafeFaucetContract, encodeNoArgs("cooldownSeconds"));
            Optional<BigInteger> nextClaimAt = rpc.callUint256(cafeFaucetContract,
                    EvmRpcClient.encodeCall("nextClaimAt", new Address(walletAddress)));
            Optional<Boolean> canClaim = rpc.callBool(cafeFaucetContract,
                    EvmRpcClient.encodeCall("canClaim", new Address(walletAddress)), null);
            status.setClaimAmount(claimAmount.map(Web3jCoffeeService::fromWei).orElse(BigDecimal.ZERO));
            status.setCooldownSeconds(cooldown.map(BigInteger::intValue).orElse(0));
            nextClaimAt.filter(n -> n.signum() > 0)
                    .ifPresent(n -> status.setNextClaimAtUtc(Instant.ofEpochSecond(n.longValue())));
            status.setCanClaim(canClaim.orElse(false));
            status.setFaucetBalance(erc20Balance(cafeFaucetContract, paymentTokenContract));
            return status;
        } catch (Exception e) {
            log.warn("Failed to read CafeFaucet status for {} from {}", walletAddress, cafeFaucetContract, e);
            status.setCanClaim(false);
            return status;
        }
    }

    @Override
    public StakingVerificationResult verifyStakingTransaction(String txHash, String expectedWallet,
                                                              BigDecimal expectedAmount, StakingTransactionType transactionType) {
        boolean isClaim = transactionType == StakingTransactionType.CLAIM;
        if (!isTransactionHash(txHash) || !isValidAddress(expectedWallet)
                || !WalletAddressRules.isConfiguredAddress(stakingPoolContract)
                || (isClaim ? !WalletAddressRules.isConfiguredAddress(coffeeCoinContract)
                    : !WalletAddressRules.isConfiguredAddress(paymentTokenContract))
                || (!isClaim && (expectedAmount == null || expectedAmount.signum() <= 0))) {
            return StakingVerificationResult.FAILED;
        }
        try {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(txHash);
            if (receipt.isEmpty() || !EvmRpcClient.isSuccessful(receipt.get())
                    || !matches(receipt.get().getTo(), stakingPoolContract)) {
                return StakingVerificationResult.FAILED;
            }
            int confirmations = confirmations(receipt.get());
            if (confirmations < chain.getMinimumConfirmations()) {
                return StakingVerificationResult.pending(confirmations, chain.getMinimumConfirmations());
            }
            var transaction = rpc.getTransaction(txHash);
            if (transaction.isEmpty() || !matches(transaction.get().getFrom(), expectedWallet)
                    || !matches(transaction.get().getTo(), stakingPoolContract)) {
                return StakingVerificationResult.FAILED;
            }
            String input = transaction.get().getInput();
            List<Log> logs = logs(receipt.get());
            if (isClaim) {
                if (!StakingCalldataDecoder.tryDecodeClaimSelector(input)) {
                    return StakingVerificationResult.FAILED;
                }
                Optional<BigInteger> reward = logs.stream()
                        .flatMap(entry -> EvmLogEvents.transfer(entry).stream())
                        .filter(t -> matches(t.contract(), coffeeCoinContract)
                                && matches(t.from(), stakingPoolContract)
                                && matches(t.to(), expectedWallet))
                        .map(EvmLogEvents.Transfer::value)
                        .findFirst();
                return reward.map(amount -> StakingVerificationResult.verified(fromWei(amount)))
                        .orElse(StakingVerificationResult.FAILED);
            }
            BigInteger expectedWei = Convert.toWei(expectedAmount, Convert.Unit.ETHER).toBigInteger();
            Optional<BigInteger> callAmount =
                    StakingCalldataDecoder.tryDecodeStakingAmount(input, transactionType);
            if (callAmount.isEmpty() || !callAmount.get().equals(expectedWei)) {
                return StakingVerificationResult.FAILED;
            }
            boolean verified = switch (transactionType) {
                case STAKE -> logs.stream().flatMap(entry -> EvmLogEvents.transfer(entry).stream())
                        .anyMatch(t -> matches(t.contract(), paymentTokenContract)
                                && matches(t.from(), expectedWallet)
                                && matches(t.to(), stakingPoolContract)
                                && t.value().equals(expectedWei));
                case UNSTAKE -> logs.stream().flatMap(entry -> EvmLogEvents.transfer(entry).stream())
                        .anyMatch(t -> matches(t.contract(), paymentTokenContract)
                                && matches(t.from(), stakingPoolContract)
                                && matches(t.to(), expectedWallet)
                                && t.value().equals(expectedWei));
                default -> false;
            };
            return verified ? StakingVerificationResult.verified(expectedAmount)
                    : StakingVerificationResult.FAILED;
        } catch (IOException e) {
            log.warn("Staking verification failed for {}", txHash, e);
            return StakingVerificationResult.FAILED;
        }
    }

    private int confirmations(TransactionReceipt receipt) {
        if (receipt.getBlockNumber() == null) {
            return 0;
        }
        try {
            Optional<BigInteger> current = rpc.getBlockNumber();
            if (current.isEmpty()) {
                return 0;
            }
            return ConfirmationCalculator.calculate(current.get(), receipt.getBlockNumber());
        } catch (IOException e) {
            return 0;
        }
    }

    private static List<Log> logs(TransactionReceipt receipt) {
        return receipt.getLogs() == null ? List.of() : receipt.getLogs();
    }

    private String detectClaimFunction(String walletAddress) {
        if (!isValidAddress(walletAddress) || !WalletAddressRules.isConfiguredAddress(stakingPoolContract)) {
            return null;
        }
        for (String name : StakingCalldataDecoder.CLAIM_FUNCTION_NAMES) {
            String selector = StakingCalldataDecoder.functionSelector(name + "()");
            if (rpc.probeCall(stakingPoolContract, walletAddress, selector)) {
                return name;
            }
        }
        return null;
    }

    private BigDecimal detectContractApr() {
        if (!WalletAddressRules.isConfiguredAddress(stakingPoolContract)) {
            return null;
        }
        try {
            Optional<BigInteger> basisPoints =
                    rpc.callUint256(stakingPoolContract, encodeNoArgs("aprBasisPoints"));
            if (basisPoints.isPresent()) {
                return new BigDecimal(basisPoints.get()).divide(new BigDecimal("100"));
            }
        } catch (IOException e) {
            log.debug("aprBasisPoints probe failed, trying rewardRate", e);
        }
        try {
            Optional<BigInteger> rewardRate =
                    rpc.callUint256(stakingPoolContract, encodeNoArgs("rewardRate"));
            Optional<BigInteger> totalStaked =
                    rpc.callUint256(stakingPoolContract, totalSupplyCall());
            if (rewardRate.isEmpty() || totalStaked.isEmpty()
                    || rewardRate.get().signum() <= 0 || totalStaked.get().signum() <= 0) {
                return null;
            }
            BigDecimal annualRewards = fromWei(rewardRate.get()).multiply(BigDecimal.valueOf(SECONDS_PER_YEAR));
            BigDecimal staked = fromWei(totalStaked.get());
            return annualRewards.divide(staked, MathContext.DECIMAL128).multiply(new BigDecimal("100"));
        } catch (IOException e) {
            return null;
        }
    }

    private BigDecimal erc20Balance(String walletAddress, String contractAddress) {
        if (!WalletAddressRules.isConfiguredAddress(contractAddress)) {
            return BigDecimal.ZERO;
        }
        try {
            Optional<BigInteger> balance = rpc.callUint256(contractAddress,
                    EvmRpcClient.encodeCall("balanceOf", new Address(walletAddress)));
            return balance.map(Web3jCoffeeService::fromWei).orElse(BigDecimal.ZERO);
        } catch (IOException e) {
            log.warn("ERC20 balance read failed", e);
            return BigDecimal.ZERO;
        }
    }

    private TransactionReceipt waitForReceipt(String transactionHash) throws IOException {
        long deadline = System.currentTimeMillis() + 120_000L;
        while (System.currentTimeMillis() < deadline) {
            Optional<TransactionReceipt> receipt = rpc.getReceipt(transactionHash);
            if (receipt.isPresent()) {
                return receipt.get();
            }
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for receipt", e);
            }
        }
        throw new IOException("Timed out waiting for transaction receipt " + transactionHash);
    }

    static String encodeNoArgs(String name) {
        return FunctionEncoder.encode(new Function(name, List.of(), List.of()));
    }

    static String totalSupplyCall() {
        return encodeNoArgs("totalSupply");
    }

    static BigDecimal fromWei(BigInteger wei) {
        return Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER);
    }

    static boolean isValidAddress(String address) {
        return address != null && !address.isBlank()
                && WalletAddressRules.isValidEthereumAddress(address);
    }

    static boolean isTransactionHash(String value) {
        return WalletAddressRules.tryNormalizeTransactionHash(value).isPresent();
    }

    static boolean matches(String actual, String expected) {
        return actual != null && !actual.isBlank() && actual.equalsIgnoreCase(expected);
    }

    static boolean isGasFundingFailure(Throwable exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase(java.util.Locale.ROOT);
        Throwable cause = exception.getCause();
        String causeMessage = cause == null || cause.getMessage() == null ? ""
                : cause.getMessage().toLowerCase(java.util.Locale.ROOT);
        return message.contains("insufficient funds") || message.contains("insufficient balance")
                || message.contains("gas required exceeds allowance")
                || causeMessage.contains("insufficient funds") || causeMessage.contains("insufficient balance")
                || causeMessage.contains("gas required exceeds allowance");
    }

    private interface ThrowingSupplier<T> {
        T get() throws IOException;
    }

    private static <T> T unchecked(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

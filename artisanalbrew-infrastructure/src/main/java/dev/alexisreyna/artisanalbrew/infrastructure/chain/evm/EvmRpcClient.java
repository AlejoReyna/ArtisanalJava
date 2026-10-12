package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Thin web3j JSON-RPC facade (mirrors Nethereum's {@code Web3} usage:
 * contract calls, receipts, current block, raw-transaction submission).
 * One instance per RPC URL; {@link #close()} releases its HTTP pool.
 */
public class EvmRpcClient implements AutoCloseable {
    private final Web3j web3j;

    public EvmRpcClient(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    EvmRpcClient(Web3j web3j) {
        this.web3j = web3j;
    }

    public Optional<BigInteger> callUint256(String contract, String data) throws IOException {
        return callUint256(contract, data, null);
    }

    public Optional<BigInteger> callUint256(String contract, String data, String from) throws IOException {
        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(from, contract, data),
                DefaultBlockParameterName.LATEST).send();
        if (response.hasError() || response.getValue() == null || response.getValue().isBlank()
                || "0x".equals(response.getValue())) {
            return Optional.empty();
        }
        try {
            String hex = response.getValue().startsWith("0x")
                    ? response.getValue().substring(2) : response.getValue();
            if (hex.length() < 64) {
                return Optional.empty();
            }
            return Optional.of(new BigInteger(hex.substring(hex.length() - 64), 16));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> callBool(String contract, String data, String from) throws IOException {
        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(from, contract, data),
                DefaultBlockParameterName.LATEST).send();
        if (response.hasError() || response.getValue() == null || response.getValue().isBlank()
                || "0x".equals(response.getValue())) {
            return Optional.empty();
        }
        try {
            String hex = response.getValue().startsWith("0x")
                    ? response.getValue().substring(2) : response.getValue();
            if (hex.length() < 64) {
                return Optional.empty();
            }
            return Optional.of(!new BigInteger(hex.substring(hex.length() - 64), 16).equals(BigInteger.ZERO));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Static-call probe: true when the node executes it without error. */
    public boolean probeCall(String contract, String from, String data) {
        try {
            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(from, contract, data),
                    DefaultBlockParameterName.LATEST).send();
            return !response.hasError();
        } catch (IOException e) {
            return false;
        }
    }

    public Optional<BigInteger> getBalance(String address) throws IOException {
        EthGetBalance response = web3j
                .ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        if (response.hasError()) {
            return Optional.empty();
        }
        return Optional.of(response.getBalance());
    }

    public Optional<BigInteger> getBlockNumber() throws IOException {
        EthBlockNumber response = web3j.ethBlockNumber().send();
        if (response.hasError()) {
            return Optional.empty();
        }
        return Optional.of(response.getBlockNumber());
    }

    public Optional<TransactionReceipt> getReceipt(String transactionHash) throws IOException {
        EthGetTransactionReceipt response = web3j.ethGetTransactionReceipt(transactionHash).send();
        if (response.hasError()) {
            return Optional.empty();
        }
        return response.getTransactionReceipt();
    }

    public Optional<org.web3j.protocol.core.methods.response.Transaction> getTransaction(String transactionHash)
            throws IOException {
        var response = web3j.ethGetTransactionByHash(transactionHash).send();
        if (response.hasError()) {
            return Optional.empty();
        }
        return response.getTransaction();
    }

    public BigInteger estimateGas(String from, String contract, String data) throws IOException {
        Transaction managed = Transaction.createFunctionCallTransaction(from, null, null, null, contract, data);
        EthEstimateGas response = web3j.ethEstimateGas(managed).send();
        if (response.hasError() || response.getAmountUsed() == null) {
            throw new IOException("Gas estimation failed: "
                    + (response.getError() == null ? "unknown" : response.getError().getMessage()));
        }
        return response.getAmountUsed();
    }

    public BigInteger getTransactionCount(String address) throws IOException {
        EthGetTransactionCount response = web3j
                .ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
        if (response.hasError()) {
            throw new IOException("Could not read transaction count");
        }
        return response.getTransactionCount();
    }

    public BigInteger getGasPrice() throws IOException {
        EthGasPrice response = web3j.ethGasPrice().send();
        if (response.hasError()) {
            throw new IOException("Could not read gas price");
        }
        return response.getGasPrice();
    }

    public String sendRawTransaction(String signedHex) throws IOException {
        EthSendTransaction response = web3j.ethSendRawTransaction(signedHex).send();
        if (response.hasError() || response.getTransactionHash() == null) {
            throw new IOException("Transaction broadcast failed: "
                    + (response.getError() == null ? "unknown" : response.getError().getMessage()));
        }
        return response.getTransactionHash();
    }

    /** Encodes {@code name(arg…)} calldata for the given ABI-typed arguments. */
    public static String encodeCall(String name, Type... args) {
        return FunctionEncoder.encode(new Function(name, Arrays.asList(args),
                Collections.<TypeReference<?>>emptyList()));
    }

    /** Parses a hex quantity ("0x…") or decimal string into a BigInteger. */
    public static BigInteger parseQuantity(String value) {
        if (value == null || value.isBlank()) {
            return BigInteger.ZERO;
        }
        String text = value.trim();
        if (text.startsWith("0x") || text.startsWith("0X")) {
            String hex = text.substring(2);
            return hex.isEmpty() ? BigInteger.ZERO : new BigInteger(hex, 16);
        }
        return new BigInteger(text);
    }

    public static boolean isSuccessful(TransactionReceipt receipt) {
        return receipt != null && receipt.getStatus() != null
                && parseQuantity(receipt.getStatus()).equals(BigInteger.ONE);
    }

    @Override
    public void close() {
        web3j.shutdown();
    }
}

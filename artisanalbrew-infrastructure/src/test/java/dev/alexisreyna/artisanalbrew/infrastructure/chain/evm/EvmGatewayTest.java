package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainCapabilities;
import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.CoffeeCoinOwnerOptions;
import dev.alexisreyna.artisanalbrew.application.config.DefaultChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingTransactionType;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingVerificationResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification paths against a canned JSON-RPC node: receipts, calldata,
 * events and confirmations flow exactly like the .NET suite asserts.
 */
class EvmGatewayTest {

    private static final String CUSTOMER = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";
    private static final String TOKEN = "0x1111111111111111111111111111111111111111";
    private static final String MARKETPLACE = "0x2222222222222222222222222222222222222222";
    private static final String POOL = "0x3333333333333333333333333333333333333333";
    private static final String VAULT = "0x4444444444444444444444444444444444444444";
    private static final String TX = "0x" + "ab".repeat(32);
    private static final BigInteger TWO_TOKENS = BigInteger.TEN.pow(18).multiply(BigInteger.TWO);

    private MockWebServer server;
    private volatile String receiptJson = "null";
    private volatile String transactionJson = "null";
    private volatile String blockNumber = "\"0x64\"";

    private static String word(BigInteger value) {
        return String.format("%064x", value);
    }

    private static String word(String address) {
        String hex = address.startsWith("0x") ? address.substring(2) : address;
        return "0".repeat(24) + hex.toLowerCase(java.util.Locale.ROOT);
    }

    private static String transferInput(String to, BigInteger amount) {
        return "0xa9059cbb" + word(to) + word(amount);
    }

    private static String log(String address, List<String> topics, String data) {
        return "{\"address\":\"" + address + "\",\"topics\":["
                + topics.stream().map(t -> "\"" + t + "\"").collect(java.util.stream.Collectors.joining(","))
                + "],\"data\":\"" + data + "\",\"blockNumber\":\"0x63\",\"transactionHash\":\"" + TX + "\","
                + "\"transactionIndex\":\"0x0\",\"blockHash\":\"0x" + "cc".repeat(32) + "\","
                + "\"logIndex\":\"0x0\",\"removed\":false}";
    }

    private static String transferTopic() {
        return EvmLogEvents.topic("Transfer(address,address,uint256)");
    }

    @BeforeEach
    void start() throws Exception {
        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String body = request.getBody().readUtf8();
                String result;
                if (body.contains("\"eth_getTransactionReceipt\"")) {
                    result = receiptJson;
                } else if (body.contains("\"eth_getTransactionByHash\"")) {
                    result = transactionJson;
                } else if (body.contains("\"eth_blockNumber\"")) {
                    result = blockNumber;
                } else if (body.contains("\"eth_getBalance\"")) {
                    result = "\"0x1\"";
                } else if (body.contains("\"eth_call\"")) {
                    result = "\"0x" + word(BigInteger.ONE) + "\"";
                } else if (body.contains("\"eth_estimateGas\"")) {
                    result = "\"0x5208\"";
                } else if (body.contains("\"eth_gasPrice\"")) {
                    result = "\"0x12\"";
                } else if (body.contains("\"eth_getTransactionCount\"")) {
                    result = "\"0x0\"";
                } else if (body.contains("\"eth_sendRawTransaction\"")) {
                    result = "\"0xminttx\"";
                } else {
                    return new MockResponse().setResponseCode(400);
                }
                return new MockResponse().setBody("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":" + result + "}");
            }
        });
        server.start();
    }

    @AfterEach
    void stop() throws Exception {
        server.shutdown();
    }

    private String rpcUrl() {
        return server.url("/").toString();
    }

    private BlockchainNetworkOptions network() {
        BlockchainNetworkOptions options = new BlockchainNetworkOptions();
        options.setNetworkName("TestNet");
        options.setChainId(11155111);
        options.setRpcUrl(rpcUrl());
        options.setExplorerUrl("https://explorer.example/");
        options.setPaymentTokenContract(TOKEN);
        options.setMarketplaceWallet(MARKETPLACE);
        options.setStakingPoolContract(POOL);
        options.setCoffeeCoinContract(TOKEN);
        options.setMinimumConfirmations(2);
        return options;
    }

    private Web3jCoffeeService coffeeService() {
        return new Web3jCoffeeService(network(), new CoffeeCoinOwnerOptions());
    }

    private static String receipt(String to, String blockNumber, String logs) {
        return "{\"transactionHash\":\"" + TX + "\",\"transactionIndex\":\"0x0\","
                + "\"blockHash\":\"0x" + "cc".repeat(32) + "\",\"blockNumber\":\"" + blockNumber + "\","
                + "\"cumulativeGasUsed\":\"0x5208\",\"gasUsed\":\"0x5208\",\"contractAddress\":null,"
                + "\"logs\":[" + logs + "],\"logsBloom\":\"0x" + "00".repeat(256) + "\","
                + "\"status\":\"0x1\",\"from\":\"" + CUSTOMER + "\",\"to\":\"" + to + "\"}";
    }

    private static String transaction(String from, String to, String input, String value) {
        return "{\"hash\":\"" + TX + "\",\"from\":\"" + from + "\",\"to\":\"" + to + "\","
                + "\"input\":\"" + input + "\",\"value\":\"" + value + "\",\"nonce\":\"0x0\","
                + "\"gas\":\"0x5208\",\"gasPrice\":\"0x12\",\"blockNumber\":\"0x63\"}";
    }

    @Test
    void paymentVerificationNeedsReceiptTxCalldataAndEvent() {
        receiptJson = receipt(TOKEN, "0x63",
                log(TOKEN, List.of(transferTopic(), "0x" + word(CUSTOMER), "0x" + word(MARKETPLACE)),
                        "0x" + word(TWO_TOKENS)));
        transactionJson = transaction(CUSTOMER, TOKEN, transferInput(MARKETPLACE, TWO_TOKENS), "0x0");
        var status = coffeeService().verifyPaymentTransaction(TX, CUSTOMER, new BigDecimal("2"));
        assertThat(status).isEqualTo(TransactionVerificationStatus.VERIFIED);

        // Wrong recipient in the event: calldata matches but the log does not.
        receiptJson = receipt(TOKEN, "0x63",
                log(TOKEN, List.of(transferTopic(), "0x" + word(CUSTOMER), "0x" + word(POOL)),
                        "0x" + word(TWO_TOKENS)));
        assertThat(coffeeService().verifyPaymentTransaction(TX, CUSTOMER, new BigDecimal("2")))
                .isEqualTo(TransactionVerificationStatus.FAILED);

        // Freshly mined: confirmations below the minimum read as pending.
        receiptJson = receipt(TOKEN, "0x64",
                log(TOKEN, List.of(transferTopic(), "0x" + word(CUSTOMER), "0x" + word(MARKETPLACE)),
                        "0x" + word(TWO_TOKENS)));
        assertThat(coffeeService().verifyPaymentTransaction(TX, CUSTOMER, new BigDecimal("2")))
                .isEqualTo(TransactionVerificationStatus.PENDING_CONFIRMATIONS);
    }

    @Test
    void stakingVerificationChecksCalldataAndDirection() {
        String stakeSelector = StakingCalldataDecoder.functionSelector("stake(uint256)");
        receiptJson = receipt(POOL, "0x63",
                log(TOKEN, List.of(transferTopic(), "0x" + word(CUSTOMER), "0x" + word(POOL)),
                        "0x" + word(TWO_TOKENS)));
        transactionJson = transaction(CUSTOMER, POOL, stakeSelector + word(TWO_TOKENS), "0x0");

        StakingVerificationResult result = coffeeService().verifyStakingTransaction(
                TX, CUSTOMER, new BigDecimal("2"), StakingTransactionType.STAKE);
        assertThat(result.verified()).isTrue();
        assertThat(result.amount()).isEqualByComparingTo("2");

        // Reversed direction must not verify as a stake.
        receiptJson = receipt(POOL, "0x63",
                log(TOKEN, List.of(transferTopic(), "0x" + word(POOL), "0x" + word(CUSTOMER)),
                        "0x" + word(TWO_TOKENS)));
        assertThat(coffeeService().verifyStakingTransaction(
                        TX, CUSTOMER, new BigDecimal("2"), StakingTransactionType.STAKE).verified())
                .isFalse();
    }

    @Test
    void nativePaymentVerification() {
        receiptJson = receipt(MARKETPLACE, "0x63", "");
        transactionJson = transaction(CUSTOMER, MARKETPLACE, "0x", "0x" + TWO_TOKENS.toString(16));
        StakingVerificationResult result =
                coffeeService().verifyNativeEthPayment(TX, CUSTOMER, new BigDecimal("2"));
        assertThat(result.verified()).isTrue();
        assertThat(result.confirmations()).isEqualTo(2);
    }

    @Test
    void liquidDepositVerification() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setEvmChainId(11155111);
        chain.setEvmChainIdHex("0xaa36a7");
        chain.setNativeCurrencySymbol("ETH");
        chain.setPublicRpcUrl(rpcUrl());
        chain.setServerRpcUrl(rpcUrl());
        chain.setMinimumConfirmations(2);
        chain.getDeployment().setLiquidVault(VAULT);
        chain.getDeployment().setCafe(TOKEN);
        chain.getDeployment().setStCafe(TOKEN);
        chain.getDeployment().setCoffee(TOKEN);
        chain.setCapabilities(new ChainCapabilities(true, true, false, false, false, false, false, false));
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey("ethereum-sepolia");
        options.setChains(List.of(chain));
        var gateway = new Web3jLiquidStakingGateway(new DefaultChainRegistry(options));

        String depositSelector = StakingCalldataDecoder.functionSelector("deposit(uint256,address)");
        String depositTopic = EvmLogEvents.topic("Deposit(address,address,uint256,uint256)");
        receiptJson = receipt(VAULT, "0x63",
                log(VAULT, List.of(depositTopic, "0x" + word(POOL), "0x" + word(CUSTOMER)),
                        "0x" + word(TWO_TOKENS) + word(BigInteger.TEN.pow(18))));
        transactionJson = transaction(CUSTOMER, VAULT, depositSelector + word(TWO_TOKENS) + word(CUSTOMER), "0x0");

        var result = gateway.verify("ethereum-sepolia", CUSTOMER, TX,
                dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingOperation.DEPOSIT,
                new BigDecimal("2"));
        assertThat(result.verified()).isTrue();
        assertThat(result.assetAmount()).isEqualByComparingTo("2");
        assertThat(result.shareAmount()).isEqualByComparingTo("1");
        assertThat(result.operationIndex()).isEqualTo(0);
    }

    @Test
    void marketplaceGatewayTreatsMissingReceiptAsPending() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setEvmChainId(11155111);
        chain.setEvmChainIdHex("0xaa36a7");
        chain.setNativeCurrencySymbol("ETH");
        chain.setPublicRpcUrl(rpcUrl());
        chain.setServerRpcUrl(rpcUrl());
        chain.setMinimumConfirmations(2);
        chain.getDeployment().setLegacyPool(MARKETPLACE);
        chain.setCapabilities(new ChainCapabilities(false, false, false, false, true, false, false, false));
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey("ethereum-sepolia");
        options.setChains(List.of(chain));
        var gateway = new Web3jMarketplacePaymentGateway(new DefaultChainRegistry(options));

        receiptJson = "null";
        var pending = gateway.verifyNativePayment("ethereum-sepolia", TX, CUSTOMER, BigDecimal.ONE);
        assertThat(pending.status())
                .isEqualTo(dev.alexisreyna.artisanalbrew.application.service.blockchain.TransactionVerificationStatus.PENDING_CONFIRMATIONS);
        assertThat(pending.confirmations()).isEqualTo(0);

        receiptJson = receipt(POOL, "0x63", "");
        transactionJson = transaction(CUSTOMER, MARKETPLACE, "0x", "0x" + BigInteger.TEN.pow(18).toString(16));
        assertThat(gateway.verifyNativePayment("ethereum-sepolia", TX, CUSTOMER, BigDecimal.ONE).verified())
                .isFalse();
    }
}

package dev.alexisreyna.artisanalbrew.application.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Port of {@code BlockchainManifestLoader} in {@code Configuration/BlockchainManifestLoader.cs}.
 *
 * <p>Committed deployment manifests promote chains to enabled by <em>replacing</em> their
 * definition; the manifest is the single source of truth for capabilities. Uses Jackson
 * ({@code System.Text.Json} equivalent) — the only JSON dependency in this module.</p>
 */
public final class BlockchainManifestLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BlockchainManifestLoader() {
    }

    public static BlockchainOptions loadDeploymentManifests(BlockchainOptions options,
                                                           String evmManifestPath, String solanaManifestPath) {
        List<ChainDefinition> chains = new ArrayList<>(options.getChains());
        for (String path : splitManifestPaths(evmManifestPath)) {
            readEvm(path).ifPresent(def -> replace(chains, def));
        }
        readSolana(solanaManifestPath).ifPresent(def -> replace(chains, def));
        BlockchainOptions result = new BlockchainOptions();
        result.setDefaultChainKey(options.getDefaultChainKey());
        result.setChains(chains);
        return result;
    }

    /**
     * Server-side bundler RPC overrides from
     * {@code ARTISANALBREW_BUNDLER_RPC_URL__{CHAIN_KEY}} and
     * {@code ARTISANALBREW_MODULAR_BUNDLER_RPC_URL__{CHAIN_KEY}} (upper-cased,
     * hyphens → underscores). Win over the manifest; never committed.
     */
    public static BlockchainOptions applyBundlerRpcUrlOverrides(BlockchainOptions options,
                                                                Function<String, String> lookupEnvironmentVariable) {
        List<ChainDefinition> chains = new ArrayList<>();
        for (ChainDefinition chain : options.getChains()) {
            String suffix = chain.getKey().toUpperCase(java.util.Locale.ROOT).replace('-', '_');
            String overrideUrl = lookupEnvironmentVariable.apply("ARTISANALBREW_BUNDLER_RPC_URL__" + suffix);
            String modularOverrideUrl =
                    lookupEnvironmentVariable.apply("ARTISANALBREW_MODULAR_BUNDLER_RPC_URL__" + suffix);
            if (!isBlank(overrideUrl)) {
                chain.setBundlerRpcUrl(overrideUrl);
            }
            if (!isBlank(modularOverrideUrl)) {
                chain.setModularBundlerRpcUrl(modularOverrideUrl);
            }
            chains.add(chain);
        }
        BlockchainOptions result = new BlockchainOptions();
        result.setDefaultChainKey(options.getDefaultChainKey());
        result.setChains(chains);
        return result;
    }

    static List<String> splitManifestPaths(String value) {
        List<String> paths = new ArrayList<>();
        if (value == null) {
            return paths;
        }
        for (String part : value.split(";")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                paths.add(trimmed);
            }
        }
        return paths;
    }

    private static void replace(List<ChainDefinition> chains, ChainDefinition definition) {
        chains.removeIf(chain -> chain.getKey().equals(definition.getKey()));
        chains.add(definition);
    }

    private static java.util.Optional<ChainDefinition> readEvm(String path) {
        JsonNode root = tryOpen(path);
        if (root == null) {
            return java.util.Optional.empty();
        }
        JsonNode addresses = root.get("addresses");
        int chainId = root.get("chainId").asInt();
        String chainKey = root.get("chainKey").asText("");
        String expectedChainKey = switch (chainId) {
            case 31337 -> "evm-local";
            case 97 -> "bsc-testnet";
            case 11155111 -> "ethereum-sepolia";
            default -> throw new IllegalStateException("Unsupported EVM manifest chain ID " + chainId + ".");
        };
        if (!chainKey.equals(expectedChainKey)) {
            throw new IllegalStateException(
                    "EVM chain ID " + chainId + " manifests must use chain key '" + expectedChainKey + "'.");
        }
        String displayName = optional(root, "displayName");
        if (displayName == null) {
            displayName = chainId == 97 ? "BSC Testnet" : chainId == 11155111 ? "Ethereum Sepolia" : "Local EVM";
        }
        JsonNode nativeNode = root.get("nativeCurrency");
        String nativeName = (nativeNode != null ? optional(nativeNode, "name") : null);
        String nativeSymbol = (nativeNode != null ? optional(nativeNode, "symbol") : null);
        if (nativeName == null) {
            nativeName = chainId == 97 ? "BNB" : "Ether";
        }
        if (nativeSymbol == null) {
            nativeSymbol = chainId == 97 ? "tBNB" : "ETH";
        }
        int nativeDecimals = nativeNode != null && nativeNode.has("decimals") ? nativeNode.get("decimals").asInt() : 18;
        String rpcUrl = optional(root, "rpcUrl");
        if (rpcUrl == null) {
            rpcUrl = chainId == 97 ? "https://97.rpc.thirdweb.com"
                    : chainId == 11155111 ? "https://ethereum-sepolia-rpc.publicnode.com" : "http://127.0.0.1:8545";
        }
        String explorerAddress = optional(root, "explorerAddressTemplate");
        String explorerTransaction = optional(root, "explorerTransactionTemplate");
        if (explorerAddress == null) {
            explorerAddress = chainId == 97 ? "https://testnet.bscscan.com/address/{0}"
                    : chainId == 11155111 ? "https://sepolia.etherscan.io/address/{0}" : "http://127.0.0.1:8545/address/{0}";
        }
        if (explorerTransaction == null) {
            explorerTransaction = chainId == 97 ? "https://testnet.bscscan.com/tx/{0}"
                    : chainId == 11155111 ? "https://sepolia.etherscan.io/tx/{0}" : "http://127.0.0.1:8545/tx/{0}";
        }

        ChainDefinition definition = new ChainDefinition();
        definition.setKey(chainKey);
        definition.setDisplayName(displayName);
        definition.setShortName(displayName);
        definition.setFamily(ChainFamily.EVM);
        definition.setEnabled(true);
        definition.setIconAsset(chainId == 97 ? "/images/bnb_logo.svg" : "/images/eth_logo.png");
        definition.setEvmChainId(chainId);
        definition.setEvmChainIdHex("0x" + Integer.toHexString(chainId));
        definition.setNativeCurrencyName(nativeName);
        definition.setNativeCurrencySymbol(nativeSymbol);
        definition.setNativeCurrencyDecimals(nativeDecimals);
        definition.setPublicRpcUrl(rpcUrl);
        definition.setBundlerRpcUrl(optional(root, "bundlerRpcUrl"));
        definition.setExplorerAddressTemplate(explorerAddress);
        definition.setExplorerTransactionTemplate(explorerTransaction);
        definition.setSortOrder(chainId == 97 ? 6 : chainId == 11155111 ? 1 : 100);

        ChainDeployment deployment = new ChainDeployment();
        deployment.setCafe(requiredAddress(addresses, "cafe"));
        deployment.setCoffee(requiredAddress(addresses, "coffee"));
        deployment.setLiquidVault(requiredAddress(addresses, "liquidVault"));
        deployment.setFaucet(requiredAddress(addresses, "faucet"));
        deployment.setStCafe(requiredAddress(addresses, "liquidVault"));
        deployment.setAgenticEscrow(optional(addresses, "erc8183Escrow"));
        deployment.setLegacyPool(optional(addresses, "legacyPool"));
        String paymentToken = optional(addresses, "paymentToken");
        deployment.setPaymentToken(paymentToken != null ? paymentToken : requiredAddress(addresses, "cafe"));
        deployment.setEntryPoint(optional(addresses, "entryPoint"));
        deployment.setAccountFactory(optional(addresses, "accountFactory"));
        deployment.setVerifyingPaymaster(optional(addresses, "verifyingPaymaster"));
        deployment.setErc8004Registry(optional(addresses, "erc8004Registry"));
        deployment.setErc7683Resolver(optional(addresses, "erc7683Resolver"));
        deployment.setModularEntryPoint(optional(addresses, "modularEntryPoint"));
        deployment.setModularAccountFactory(optional(addresses, "modularSimpleFactory"));
        deployment.setDelegationManager(optional(addresses, "delegationManager"));
        deployment.setHybridDeleGatorImplementation(optional(addresses, "hybridDeleGatorImplementation"));
        deployment.setAllowedTargetsEnforcer(optional(addresses, "allowedTargetsEnforcer"));
        deployment.setAllowedMethodsEnforcer(optional(addresses, "allowedMethodsEnforcer"));
        deployment.setExactCalldataEnforcer(optional(addresses, "exactCalldataEnforcer"));
        deployment.setLimitedCallsEnforcer(optional(addresses, "limitedCallsEnforcer"));
        deployment.setNonceEnforcer(optional(addresses, "nonceEnforcer"));
        deployment.setTimestampEnforcer(optional(addresses, "timestampEnforcer"));
        JsonNode aa = root.get("accountAbstraction");
        if (aa != null) {
            deployment.setModularAccountType(optional(aa, "modularAccountType"));
            deployment.setModularFrameworkRevision(optional(aa, "modularFrameworkRevision"));
        }
        deployment.setStartBlockOrSlot(readLong(root, "deployBlock"));
        // Jackson returns "" for missing nodes via asText("") — normalize to null-free "".
        normalizeBlanks(deployment);
        definition.setDeployment(deployment);

        definition.setCapabilities(new ChainCapabilities(
                readCapabilityFlag(root, "walletLogin"),
                readCapabilityFlag(root, "liquidStaking"),
                readCapabilityFlag(root, "legacyExit"),
                readCapabilityFlag(root, "faucet"),
                readCapabilityFlag(root, "marketplacePayment"),
                readCapabilityFlag(root, "rewardMinting"),
                readCapabilityFlag(root, "agenticCommerce"),
                readCapabilityFlag(root, "agenticSessionPayments")));
        return java.util.Optional.of(definition);
    }

    private static java.util.Optional<ChainDefinition> readSolana(String path) {
        JsonNode root = tryOpen(path);
        if (root == null) {
            return java.util.Optional.empty();
        }
        if (!"1".equals(root.get("schemaVersion").asText())) {
            throw new IllegalStateException("Unsupported Solana manifest schema version.");
        }
        String cluster = required(root, "cluster");
        if (!cluster.equals("localnet") && !cluster.equals("devnet") && !cluster.equals("testnet")) {
            throw new IllegalStateException("Only Solana localnet, Devnet, and Testnet deployment manifests are supported.");
        }
        String chainKey = required(root, "chainKey");
        String expectedChainKey = "solana-" + cluster;
        if (!chainKey.equals(expectedChainKey)) {
            throw new IllegalStateException("Solana " + cluster + " manifests must use chain key '" + expectedChainKey + "'.");
        }
        String statePda = required(root, "statePda");
        String authorityPda = required(root, "authorityPda");
        if (!statePda.equals(authorityPda)) {
            throw new IllegalStateException("The current program requires the vault state PDA to also be the token authority PDA.");
        }
        int cafeDecimals = root.get("cafeDecimals").asInt();
        int stCafeDecimals = root.get("stCafeDecimals").asInt();
        int coffeeDecimals = root.get("coffeeDecimals").asInt();
        if (cafeDecimals < 0 || cafeDecimals > 9 || stCafeDecimals != cafeDecimals || coffeeDecimals < 0 || coffeeDecimals > 9) {
            throw new IllegalStateException("The Solana manifest contains unsupported token decimals.");
        }

        ChainDefinition definition = new ChainDefinition();
        definition.setKey(chainKey);
        String displayName = cluster.equals("localnet") ? "Solana Localnet"
                : cluster.equals("devnet") ? "Solana Devnet" : "Solana Testnet";
        definition.setDisplayName(displayName);
        definition.setShortName(displayName);
        definition.setFamily(ChainFamily.SOLANA);
        definition.setEnabled(true);
        definition.setIconAsset("/images/solana_logo.svg");
        definition.setSolanaCluster(cluster);
        definition.setNativeCurrencyName("Solana");
        definition.setNativeCurrencySymbol("SOL");
        definition.setNativeCurrencyDecimals(9);
        definition.setPublicRpcUrl(required(root, "rpcUrl"));
        String explorerCluster = cluster.equals("localnet") ? "custom" : cluster;
        definition.setExplorerAddressTemplate("https://explorer.solana.com/address/{0}?cluster=" + explorerCluster);
        definition.setExplorerTransactionTemplate("https://explorer.solana.com/tx/{0}?cluster=" + explorerCluster);
        definition.setSolanaCommitment("finalized");
        definition.setSortOrder(cluster.equals("localnet") ? 101 : cluster.equals("devnet") ? 9 : 10);

        ChainDeployment deployment = new ChainDeployment();
        deployment.setProgram(required(root, "programId"));
        deployment.setVaultPda(statePda);
        deployment.setAuthorityPda(authorityPda);
        deployment.setCafe(required(root, "cafeMint"));
        deployment.setStCafe(required(root, "stCafeMint"));
        deployment.setCoffee(required(root, "coffeeMint"));
        deployment.setCafeCustody(required(root, "cafeCustody"));
        deployment.setCoffeeCustody(required(root, "coffeeCustody"));
        deployment.setAdmin(required(root, "administrator"));
        deployment.setTokenProgram(required(root, "tokenProgram"));
        deployment.setToken2022Program(required(root, "token2022Program"));
        deployment.setCafeDecimals(cafeDecimals);
        deployment.setStCafeDecimals(stCafeDecimals);
        deployment.setCoffeeDecimals(coffeeDecimals);
        deployment.setStartBlockOrSlot(root.get("deploymentSlot").asLong());
        definition.setDeployment(deployment);

        definition.setCapabilities(new ChainCapabilities(
                true, true, false, readCapabilityFlag(root, "faucet"), false, true, false, false));
        return java.util.Optional.of(definition);
    }

    private static String required(JsonNode root, String name) {
        JsonNode node = root.get(name);
        String value = node == null ? null : node.asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Manifest property '" + name + "' is required.");
        }
        return value;
    }

    private static String requiredAddress(JsonNode addresses, String name) {
        JsonNode node = addresses == null ? null : addresses.get(name);
        String value = node == null ? null : node.asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Manifest addresses property '" + name + "' is required.");
        }
        return value;
    }

    private static String optional(JsonNode node, String name) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode child = node.get(name);
        if (child == null || !child.isTextual()) {
            return null;
        }
        return child.asText();
    }

    private static boolean readCapabilityFlag(JsonNode root, String name) {
        JsonNode capabilities = root.get("capabilities");
        if (capabilities == null || !capabilities.isObject()) {
            return false;
        }
        JsonNode flag = capabilities.get(name);
        if (flag == null || flag.isMissingNode() || flag.isNull()) {
            return false;
        }
        if (!flag.isBoolean()) {
            throw new IllegalStateException("Manifest capability '" + name + "' must be a boolean.");
        }
        return flag.asBoolean();
    }

    private static long readLong(JsonNode root, String name) {
        JsonNode node = root.get(name);
        if (node == null) {
            throw new IllegalStateException("Manifest property '" + name + "' must be an integer.");
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        if (node.isTextual()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Manifest property '" + name + "' must be an integer.");
            }
        }
        throw new IllegalStateException("Manifest property '" + name + "' must be an integer.");
    }

    private static JsonNode tryOpen(String path) {
        Path resolved = tryResolveManifestPath(path);
        if (resolved == null) {
            return null;
        }
        try {
            return MAPPER.readTree(Files.readAllBytes(resolved));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Relative paths probe the working directory and each ancestor (mirrors
     * {@code LocalDotEnvLoader}); absolute paths are used as-is. Missing files
     * load nothing — the registry keeps its stub.
     */
    static Path tryResolveManifestPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        Path candidate = Paths.get(path);
        if (candidate.isAbsolute()) {
            return Files.isRegularFile(candidate) ? candidate : null;
        }
        Path directory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (directory != null) {
            Path resolved = directory.resolve(path);
            if (Files.isRegularFile(resolved)) {
                return resolved;
            }
            directory = directory.getParent();
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Jackson's {@code asText("")} leaves "" for absent nodes — matches the .NET {@code ?? string.Empty} shape. */
    private static void normalizeBlanks(ChainDeployment deployment) {
        if (deployment.getAgenticEscrow() == null) {
            deployment.setAgenticEscrow("");
        }
        if (deployment.getLegacyPool() == null) {
            deployment.setLegacyPool("");
        }
        if (deployment.getPaymentToken() == null) {
            deployment.setPaymentToken("");
        }
        if (deployment.getEntryPoint() == null) {
            deployment.setEntryPoint("");
        }
        if (deployment.getAccountFactory() == null) {
            deployment.setAccountFactory("");
        }
        if (deployment.getVerifyingPaymaster() == null) {
            deployment.setVerifyingPaymaster("");
        }
        if (deployment.getErc8004Registry() == null) {
            deployment.setErc8004Registry("");
        }
        if (deployment.getErc7683Resolver() == null) {
            deployment.setErc7683Resolver("");
        }
        if (deployment.getModularEntryPoint() == null) {
            deployment.setModularEntryPoint("");
        }
        if (deployment.getModularAccountFactory() == null) {
            deployment.setModularAccountFactory("");
        }
        if (deployment.getDelegationManager() == null) {
            deployment.setDelegationManager("");
        }
        if (deployment.getHybridDeleGatorImplementation() == null) {
            deployment.setHybridDeleGatorImplementation("");
        }
        if (deployment.getAllowedTargetsEnforcer() == null) {
            deployment.setAllowedTargetsEnforcer("");
        }
        if (deployment.getAllowedMethodsEnforcer() == null) {
            deployment.setAllowedMethodsEnforcer("");
        }
        if (deployment.getExactCalldataEnforcer() == null) {
            deployment.setExactCalldataEnforcer("");
        }
        if (deployment.getLimitedCallsEnforcer() == null) {
            deployment.setLimitedCallsEnforcer("");
        }
        if (deployment.getNonceEnforcer() == null) {
            deployment.setNonceEnforcer("");
        }
        if (deployment.getTimestampEnforcer() == null) {
            deployment.setTimestampEnforcer("");
        }
        if (deployment.getModularAccountType() == null) {
            deployment.setModularAccountType("");
        }
        if (deployment.getModularFrameworkRevision() == null) {
            deployment.setModularFrameworkRevision("");
        }
    }
}

package dev.alexisreyna.artisanalbrew.application.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Port of {@code ChainRegistry} validation in {@code Configuration/IChainRegistry.cs}.
 * Immutable snapshot built from {@link BlockchainOptions}; empty chains fall back
 * to {@link ChainDefinitionDefaults#publicChains()}.
 */
public final class DefaultChainRegistry implements ChainRegistry {
    private final String defaultChainKey;
    private final List<ChainDefinition> all;
    private final Map<String, ChainDefinition> byKey;

    public DefaultChainRegistry(BlockchainOptions options) {
        List<ChainDefinition> chains = options.getChains().isEmpty()
                ? ChainDefinitionDefaults.publicChains()
                : options.getChains();
        validate(options.getDefaultChainKey(), chains);
        List<ChainDefinition> sorted = new ArrayList<>(chains);
        sorted.sort(Comparator.comparingInt(ChainDefinition::getSortOrder));
        this.all = List.copyOf(sorted);
        Map<String, ChainDefinition> map = new HashMap<>();
        for (ChainDefinition chain : this.all) {
            map.put(chain.getKey(), chain);
        }
        this.byKey = Map.copyOf(map);
        this.defaultChainKey = options.getDefaultChainKey();
    }

    @Override
    public String defaultChainKey() {
        return defaultChainKey;
    }

    @Override
    public List<ChainDefinition> all() {
        return all;
    }

    @Override
    public Optional<ChainDefinition> tryGet(String key) {
        return Optional.ofNullable(byKey.get(key));
    }

    static void validate(String defaultKey, List<ChainDefinition> chains) {
        if (chains.isEmpty()) {
            throw new IllegalStateException("At least one blockchain must be configured.");
        }
        if (chains.stream().noneMatch(c -> c.getKey().equals(defaultKey))) {
            throw new IllegalStateException("Default chain '" + defaultKey + "' is not configured.");
        }
        if (chains.stream().anyMatch(c -> c.getKey() == null || c.getKey().isBlank() || c.getKey().length() > 64)) {
            throw new IllegalStateException("Chain keys must be non-empty and <= 64 characters.");
        }
        Set<String> keys = new HashSet<>();
        for (ChainDefinition c : chains) {
            if (!keys.add(c.getKey())) {
                throw new IllegalStateException("Chain keys must be unique.");
            }
        }
        Set<Integer> evmIds = new HashSet<>();
        for (ChainDefinition c : chains) {
            if (c.getFamily() == ChainFamily.EVM && c.getEvmChainId() != null && !evmIds.add(c.getEvmChainId())) {
                throw new IllegalStateException("EVM chain IDs must be unique.");
            }
        }
        for (ChainDefinition chain : chains) {
            if (!isHttpUrl(chain.getPublicRpcUrl())) {
                throw new IllegalStateException("Chain '" + chain.getKey() + "' has an invalid public RPC URL.");
            }
            if (chain.getServerRpcUrl() != null && !chain.getServerRpcUrl().isBlank() && !isHttpUrl(chain.getServerRpcUrl())) {
                throw new IllegalStateException("Chain '" + chain.getKey() + "' has an invalid server RPC URL.");
            }
            if (chain.getFamily() == ChainFamily.EVM
                    && (chain.getEvmChainId() == null || chain.getEvmChainIdHex() == null || chain.getEvmChainIdHex().isBlank())) {
                throw new IllegalStateException("EVM chain '" + chain.getKey() + "' must define chain ID and hex ID.");
            }
            if (chain.getFamily() == ChainFamily.SOLANA
                    && (chain.getSolanaCluster() == null || chain.getSolanaCluster().isBlank())) {
                throw new IllegalStateException("Solana chain '" + chain.getKey() + "' must define a cluster.");
            }
            ChainDeployment d = chain.getDeployment();
            ChainCapabilities caps = chain.getCapabilities();
            if (chain.getFamily() == ChainFamily.EVM && caps.liquidStaking() && isBlank(d.getLiquidVault())) {
                throw new IllegalStateException("Chain '" + chain.getKey() + "' enables liquid staking without a vault deployment.");
            }
            if (caps.legacyExit() && isBlank(d.getLegacyPool())) {
                throw new IllegalStateException("Chain '" + chain.getKey() + "' enables legacy exit without a pool deployment.");
            }
            if (caps.marketplacePayment() && chain.getFamily() == ChainFamily.EVM
                    && isBlank(d.getAgenticEscrow()) && isBlank(d.getLegacyPool())) {
                throw new IllegalStateException("Chain '" + chain.getKey()
                        + "' enables marketplace payment without an escrow or legacy pool deployment.");
            }
            if (caps.agenticSessionPayments() && chain.getFamily() == ChainFamily.EVM
                    && (isBlank(d.getDelegationManager()) || isBlank(d.getHybridDeleGatorImplementation())
                        || isBlank(d.getModularEntryPoint()))) {
                throw new IllegalStateException("Chain '" + chain.getKey()
                        + "' enables agentic session payments without a DelegationManager, HybridDeleGator, and modular EntryPoint deployment.");
            }
            if (chain.getFamily() == ChainFamily.SOLANA && caps.liquidStaking() && isBlank(d.getProgram())) {
                throw new IllegalStateException("Solana chain '" + chain.getKey() + "' enables staking without a program deployment.");
            }
            if (chain.getFamily() == ChainFamily.SOLANA && caps.faucet()
                    && (isBlank(d.getCafe()) || isBlank(d.getAdmin()))) {
                throw new IllegalStateException("Solana chain '" + chain.getKey()
                        + "' enables the faucet without a CAFE mint and administrator authority.");
            }
            if (chain.isEnabled() && chain.getFamily() == ChainFamily.SOLANA && caps.liquidStaking()) {
                String[] addresses = {d.getProgram(), d.getVaultPda(), d.getAuthorityPda(), d.getCafe(),
                        d.getStCafe(), d.getCoffee(), d.getCafeCustody(), d.getCoffeeCustody(), d.getAdmin(),
                        d.getTokenProgram(), d.getToken2022Program()};
                for (String address : addresses) {
                    if (!SolanaAddressRules.isPublicKey(address)) {
                        throw new IllegalStateException("Enabled Solana chain '" + chain.getKey()
                                + "' contains an invalid deployment public key.");
                    }
                }
                if (!SolanaAddressRules.TOKEN_PROGRAM.equals(d.getTokenProgram())
                        || !SolanaAddressRules.TOKEN_2022_PROGRAM.equals(d.getToken2022Program())) {
                    throw new IllegalStateException("Enabled Solana chain '" + chain.getKey()
                            + "' must use the canonical SPL Token and Token-2022 programs.");
                }
                if (d.getCafeDecimals() < 0 || d.getCafeDecimals() > 9
                        || d.getStCafeDecimals() != d.getCafeDecimals()
                        || d.getCoffeeDecimals() < 0 || d.getCoffeeDecimals() > 9) {
                    throw new IllegalStateException("Enabled Solana chain '" + chain.getKey()
                            + "' contains unsupported token decimals.");
                }
            }
        }
    }

    private static boolean isHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            String scheme = URI.create(url).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package dev.alexisreyna.artisanalbrew.application.config;

import java.util.ArrayList;
import java.util.List;

/** Port of {@code BlockchainOptions} + {@code ChainDefinitionDefaults} in {@code Configuration/BlockchainOptions.cs}. */
public final class BlockchainOptions {
    public static final String SECTION_NAME = "Blockchain";

    private String defaultChainKey = "ethereum-sepolia";
    private List<ChainDefinition> chains = new ArrayList<>();

    public String getDefaultChainKey() { return defaultChainKey; }
    public void setDefaultChainKey(String v) { this.defaultChainKey = v; }
    public List<ChainDefinition> getChains() { return chains; }
    public void setChains(List<ChainDefinition> v) { this.chains = v; }

    public static BlockchainOptions createDefaults() {
        BlockchainOptions options = new BlockchainOptions();
        options.setDefaultChainKey("ethereum-sepolia");
        options.setChains(ChainDefinitionDefaults.publicChains());
        return options;
    }
}

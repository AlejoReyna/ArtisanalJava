package dev.alexisreyna.artisanalbrew.application.config;

import java.util.ArrayList;
import java.util.List;

/** Port of {@code ChainDefinitionDefaults} in {@code Configuration/BlockchainOptions.cs}. */
public final class ChainDefinitionDefaults {
    private ChainDefinitionDefaults() {
    }

    public static List<ChainDefinition> publicChains() {
        List<ChainDefinition> chains = new ArrayList<>();
        chains.add(evm("ethereum-sepolia", "Ethereum Sepolia", "ETH Sepolia", 11155111, "ETH",
                "https://ethereum-sepolia-rpc.publicnode.com", "https://sepolia.etherscan.io", 1, true, true, "/images/eth_logo.png"));
        chains.add(evm("hedera-testnet", "Hedera Testnet", "Hedera", 296, "HBAR",
                "https://296.rpc.thirdweb.com", "https://hashscan.io/testnet", 2, false, false, "/images/eth_logo.png"));
        chains.add(evm("avalanche-fuji", "Avalanche Fuji", "Fuji", 43113, "AVAX",
                "https://43113.rpc.thirdweb.com", "https://testnet.snowtrace.io", 3, false, false, "/images/eth_logo.png"));
        chains.add(evm("linea-sepolia", "Linea Sepolia", "Linea", 59141, "ETH",
                "https://59141.rpc.thirdweb.com", "https://sepolia.lineascan.build", 4, false, false, "/images/eth_logo.png"));
        chains.add(evm("base-sepolia", "Base Sepolia", "Base", 84532, "ETH",
                "https://84532.rpc.thirdweb.com", "https://sepolia.basescan.org", 5, false, false, "/images/eth_logo.png"));
        chains.add(evm("bsc-testnet", "BNB Smart Chain Testnet", "BSC Testnet", 97, "tBNB",
                "https://97.rpc.thirdweb.com", "https://testnet.bscscan.com", 6, false, false, "/images/bnb_logo.svg"));
        chains.add(evm("monad-testnet", "Monad Testnet", "Monad", 10143, "MON",
                "https://10143.rpc.thirdweb.com", "https://monad-testnet.socialscan.io", 7, false, false, "/images/eth_logo.png"));
        chains.add(evm("arbitrum-sepolia", "Arbitrum Sepolia", "Arbitrum", 421614, "ETH",
                "https://421614.rpc.thirdweb.com", "https://sepolia.arbiscan.io", 8, false, false, "/images/eth_logo.png"));

        ChainDefinition solana = new ChainDefinition();
        solana.setKey("solana-devnet");
        solana.setDisplayName("Solana Devnet");
        solana.setShortName("Solana Devnet");
        solana.setFamily(ChainFamily.SOLANA);
        solana.setEnabled(false);
        solana.setIconAsset("/images/solana_logo.svg");
        solana.setSolanaCluster("devnet");
        solana.setNativeCurrencyName("Solana");
        solana.setNativeCurrencySymbol("SOL");
        solana.setNativeCurrencyDecimals(9);
        solana.setPublicRpcUrl("https://api.devnet.solana.com");
        solana.setExplorerAddressTemplate("https://explorer.solana.com/address/{0}?cluster=devnet");
        solana.setExplorerTransactionTemplate("https://explorer.solana.com/tx/{0}?cluster=devnet");
        solana.setSortOrder(9);
        solana.setCapabilities(new ChainCapabilities());
        chains.add(solana);
        return chains;
    }

    private static ChainDefinition evm(String key, String name, String shortName, int chainId, String symbol,
                                       String rpc, String explorer, int order, boolean legacy, boolean enabled, String iconAsset) {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey(key);
        chain.setDisplayName(name);
        chain.setShortName(shortName);
        chain.setFamily(ChainFamily.EVM);
        chain.setEvmChainId(chainId);
        chain.setEvmChainIdHex("0x" + Integer.toHexString(chainId));
        chain.setNativeCurrencyName(symbol);
        chain.setNativeCurrencySymbol(symbol);
        chain.setPublicRpcUrl(rpc);
        String base = explorer.endsWith("/") ? explorer.substring(0, explorer.length() - 1) : explorer;
        chain.setExplorerAddressTemplate(base + "/address/{0}");
        chain.setExplorerTransactionTemplate(base + "/tx/{0}");
        chain.setSortOrder(order);
        chain.setEnabled(enabled);
        chain.setIconAsset(iconAsset);
        if (legacy) {
            ChainDeployment deployment = new ChainDeployment();
            deployment.setCafe("0x15DbED39271D2788a9Be63ffB34C8E2DdED8754A");
            deployment.setCoffee("0x4056E7F5FD1584C3db6223c9483761Dcb30Bf21C");
            deployment.setLegacyPool("0x932d50E20F917B9BbBe2C40F30D43BCef0e93F90");
            deployment.setFaucet("0xBD1517529BB0BA20c43b4E39323C70058FADe86D");
            chain.setDeployment(deployment);
        }
        chain.setCapabilities(new ChainCapabilities(true, false, legacy, legacy, legacy, legacy, false, false));
        return chain;
    }
}

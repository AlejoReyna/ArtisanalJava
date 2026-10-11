package dev.alexisreyna.artisanalbrew.application.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loads the REAL committed manifests from the .NET checkout (sibling
 * {@code ../ArtisanalBrew/deployments}) — the strongest parity check in this
 * module: the manifests must satisfy the ported {@link DefaultChainRegistry}.
 */
class BlockchainManifestLoaderTest {

    private static String manifest(String name) {
        Path moduleDir = Paths.get(System.getProperty("user.dir"));
        Path file = moduleDir.getParent().resolveSibling("ArtisanalBrew").resolve("deployments").resolve(name);
        assertThat(Files.isRegularFile(file))
                .as("expected .NET checkout manifest at %s", file)
                .isTrue();
        return file.toAbsolutePath().toString();
    }

    @Test
    void realManifestsPromoteChainsAndPassRegistryValidation() {
        BlockchainOptions options = BlockchainOptions.createDefaults();
        BlockchainOptions loaded = BlockchainManifestLoader.loadDeploymentManifests(
                options,
                manifest("ethereum-sepolia.json") + ";" + manifest("bsc-testnet.json"),
                manifest("solana-devnet.json"));

        // Replace-by-key: still 9 chains, but the three stubs are now deployments.
        assertThat(loaded.getChains()).hasSize(9);
        ChainRegistry registry = new DefaultChainRegistry(loaded);

        ChainDefinition sepolia = registry.getRequired("ethereum-sepolia");
        assertThat(sepolia.getDeployment().getCafe()).isEqualTo("0x15DbED39271D2788a9Be63ffB34C8E2DdED8754A");
        assertThat(sepolia.getDeployment().getLiquidVault()).isNotBlank();
        assertThat(sepolia.getCapabilities().marketplacePayment()).isTrue();
        assertThat(sepolia.getCapabilities().liquidStaking()).isTrue();
        assertThat(sepolia.getDeployment().getStartBlockOrSlot()).isPositive();

        ChainDefinition bsc = registry.getRequired("bsc-testnet");
        assertThat(bsc.getEvmChainId()).isEqualTo(97);

        ChainDefinition solana = registry.getRequired("solana-devnet");
        assertThat(solana.getDeployment().getProgram()).isNotBlank();
        assertThat(solana.getDeployment().getCafeDecimals()).isEqualTo(9);
        assertThat(solana.getSolanaCommitment()).isEqualTo("finalized");
    }

    @Test
    void missingManifestsLeaveOptionsUntouched() {
        BlockchainOptions options = BlockchainOptions.createDefaults();
        BlockchainOptions loaded = BlockchainManifestLoader.loadDeploymentManifests(options, null, "/nonexistent/x.json");
        assertThat(loaded.getChains()).hasSize(9);
    }

    @Test
    void bundlerOverridesWinOverManifest() {
        BlockchainOptions options = BlockchainOptions.createDefaults();
        Map<String, String> env = new HashMap<>();
        env.put("ARTISANALBREW_BUNDLER_RPC_URL__ETHEREUM_SEPOLIA", "https://bundler.example/rpc");
        BlockchainOptions overridden =
                BlockchainManifestLoader.applyBundlerRpcUrlOverrides(options, env::get);
        ChainDefinition sepolia = overridden.getChains().stream()
                .filter(c -> c.getKey().equals("ethereum-sepolia")).findFirst().orElseThrow();
        assertThat(sepolia.getBundlerRpcUrl()).isEqualTo("https://bundler.example/rpc");
        // Untouched chains keep whatever they had (null here).
        ChainDefinition fuji = overridden.getChains().stream()
                .filter(c -> c.getKey().equals("avalanche-fuji")).findFirst().orElseThrow();
        assertThat(fuji.getBundlerRpcUrl()).isNull();
    }
}

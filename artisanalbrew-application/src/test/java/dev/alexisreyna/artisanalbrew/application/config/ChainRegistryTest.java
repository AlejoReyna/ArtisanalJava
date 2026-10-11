package dev.alexisreyna.artisanalbrew.application.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChainRegistryTest {

    @Test
    void defaultsValidateAndResolve() {
        ChainRegistry registry = new DefaultChainRegistry(BlockchainOptions.createDefaults());
        assertThat(registry.defaultChainKey()).isEqualTo("ethereum-sepolia");
        assertThat(registry.all()).hasSize(9);
        assertThat(registry.getRequired("ethereum-sepolia").getEvmChainId()).isEqualTo(11155111);
        // Disabled chains resolve via tryGet but throw on getRequired.
        assertThat(registry.tryGet("solana-devnet")).isPresent();
        assertThatThrownBy(() -> registry.getRequired("solana-devnet"))
                .isInstanceOf(java.util.NoSuchElementException.class);
        assertThatThrownBy(() -> registry.getRequired("nope"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void rejectsCapabilityWithoutDeployment() {
        BlockchainOptions options = BlockchainOptions.createDefaults();
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("evm-bad");
        chain.setFamily(ChainFamily.EVM);
        chain.setEvmChainId(12345);
        chain.setEvmChainIdHex("0x3039");
        chain.setPublicRpcUrl("https://example.com");
        chain.setSortOrder(99);
        chain.setCapabilities(new ChainCapabilities(false, true, false, false, false, false, false, false));
        options.setChains(List.of(chain));
        options.setDefaultChainKey("evm-bad");
        assertThatThrownBy(() -> new DefaultChainRegistry(options))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without a vault deployment");
    }

    @Test
    void rejectsDuplicateKeysAndUnknownDefault() {
        BlockchainOptions dupes = BlockchainOptions.createDefaults();
        dupes.setChains(List.of(dupes.getChains().get(0), dupes.getChains().get(0)));
        assertThatThrownBy(() -> new DefaultChainRegistry(dupes))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unique");

        BlockchainOptions badDefault = BlockchainOptions.createDefaults();
        badDefault.setDefaultChainKey("missing");
        assertThatThrownBy(() -> new DefaultChainRegistry(badDefault))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void failClosedOptionsStayDisabled() {
        assertThat(new CrossChainSolverOptions().canPrice()).isFalse();
        assertThat(new CrossChainSolverOptions().canOperate()).isFalse();
        assertThat(new SponsorshipPolicyOptions().canSign()).isFalse();
        assertThat(new SponsorshipPolicyOptions().isTargetAllowed("0xabc")).isFalse();
    }
}

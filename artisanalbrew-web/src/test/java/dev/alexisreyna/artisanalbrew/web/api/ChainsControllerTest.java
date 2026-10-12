package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.web.chain.SelectedChainAccessor;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChainsController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class ChainsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ChainRegistry registry;

    @MockitoBean
    private SelectedChainAccessor selected;

    private static ChainDefinition sepolia() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setShortName("ETH Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setEvmChainId(11155111);
        chain.setEvmChainIdHex("0xaa36a7");
        chain.setNativeCurrencyName("Sepolia ETH");
        chain.setNativeCurrencySymbol("ETH");
        chain.setPublicRpcUrl("https://ethereum-sepolia-rpc.publicnode.com");
        chain.setBundlerRpcUrl("https://secret-bundler.example/rpc");
        chain.setServerRpcUrl("https://secret-server.example/rpc");
        chain.getDeployment().setCafe("0xcafe");
        return chain;
    }

    @Test
    void listsEnabledChainsWithoutServerSecrets() throws Exception {
        ChainDefinition disabled = sepolia();
        disabled.setKey("x");
        disabled.setEnabled(false);
        when(registry.all()).thenReturn(List.of(sepolia(), disabled));
        when(registry.defaultChainKey()).thenReturn("ethereum-sepolia");
        when(selected.selectedChainKey()).thenReturn("ethereum-sepolia");

        mvc.perform(get("/api/chains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultChainKey").value("ethereum-sepolia"))
                .andExpect(jsonPath("$.selectedChainKey").value("ethereum-sepolia"))
                .andExpect(jsonPath("$.chains.length()").value(1))
                .andExpect(jsonPath("$.chains[0].deployments.cafe").value("0xcafe"))
                .andExpect(jsonPath("$.chains[0].bundlerRpcUrl").doesNotExist())
                .andExpect(jsonPath("$.chains[0].serverRpcUrl").doesNotExist());
    }

    @Test
    void selectUnknownChainIs400() throws Exception {
        when(selected.select("nope")).thenReturn(false);
        mvc.perform(post("/api/chains/select").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chainKey\":\"nope\"}"))
                .andExpect(status().isBadRequest());

        ChainDefinition chain = sepolia();
        when(selected.select("ethereum-sepolia")).thenReturn(true);
        when(selected.selectedChain()).thenReturn(chain);
        mvc.perform(post("/api/chains/select").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chainKey\":\"  ethereum-sepolia \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedChainKey").value("ethereum-sepolia"))
                .andExpect(jsonPath("$.family").value("EVM"));
    }
}

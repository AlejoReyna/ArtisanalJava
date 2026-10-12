package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CafeFaucetStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetClaimResult;
import dev.alexisreyna.artisanalbrew.web.chain.SelectedChainAccessor;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolanaFaucetController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class SolanaFaucetControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SolanaFaucetService faucet;
    @MockitoBean
    private ChainRegistry chains;
    @MockitoBean
    private SelectedChainAccessor selectedChain;

    private static final String PUBKEY = "C7g7g34QzvmAiP4HMmdjWLgfV9Y8FSF4GcAXK97HLQEg";

    private static ChainDefinition devnet() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("solana-devnet");
        chain.setFamily(ChainFamily.SOLANA);
        chain.setEnabled(true);
        chain.setCapabilities(new dev.alexisreyna.artisanalbrew.application.config.ChainCapabilities(
                true, true, false, true, false, true, false, false));
        return chain;
    }

    @Test
    void statusAndClaim() throws Exception {
        when(chains.tryGet("solana-devnet")).thenReturn(Optional.of(devnet()));
        CafeFaucetStatus faucetStatus = new CafeFaucetStatus();
        faucetStatus.setCanClaim(true);
        faucetStatus.setClaimAmount(new BigDecimal("100"));
        when(faucet.getStatus("solana-devnet", PUBKEY)).thenReturn(faucetStatus);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", PUBKEY);
        mvc.perform(get("/faucet/api/solana/status")
                        .param("chainKey", "solana-devnet").param("walletIdentifier", PUBKEY)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canClaim").value(true));

        mvc.perform(get("/faucet/api/solana/status")
                        .param("chainKey", "solana-devnet").param("walletIdentifier", PUBKEY))
                .andExpect(status().isUnauthorized());

        when(selectedChain.selectedChainKey()).thenReturn("solana-devnet");
        when(faucet.claim(anyString(), anyString()))
                .thenReturn(new SolanaFaucetClaimResult(true, "sig", "https://explorer.example/tx/sig",
                        new BigDecimal("100"), null, null));
        mvc.perform(post("/faucet/api/solana/claim").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chainKey\":\"solana-devnet\",\"walletIdentifier\":\"" + PUBKEY + "\"}")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

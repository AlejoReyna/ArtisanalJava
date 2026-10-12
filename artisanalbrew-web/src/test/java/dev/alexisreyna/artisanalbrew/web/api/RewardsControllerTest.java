package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.RewardClaimModels;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.RewardClaimService;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintCommand;
import dev.alexisreyna.artisanalbrew.application.service.rewards.LoyaltyMintResult;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardsController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class RewardsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RewardClaimService rewards;
    @MockitoBean
    private LoyaltyMintService loyalty;
    @MockitoBean
    private BlockchainNetworkOptions chain;

    private static final String WALLET = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";
    private static final String TX = "0x" + "ab".repeat(32);

    @Test
    @WithMockUser
    void claimableAndClaim() throws Exception {
        RewardClaimModels.Status status = new RewardClaimModels.Status();
        status.setCanClaimToday(true);
        when(rewards.getClaimStatus(WALLET)).thenReturn(status);
        mvc.perform(get("/rewards/api/claimable").param("walletAddress", WALLET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canClaimToday").value(true));
        mvc.perform(get("/rewards/api/claimable").param("walletAddress", "nope"))
                .andExpect(status().isBadRequest());

        RewardClaimModels.Result ok = new RewardClaimModels.Result();
        ok.setSuccess(true);
        when(rewards.claimDailyReward(WALLET)).thenReturn(ok);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        mvc.perform(post("/rewards/api/claim").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\"}")
                        .session(session))
                .andExpect(status().isOk());

        mvc.perform(post("/rewards/api/claim").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mintLoyaltyMapsStatuses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        String body = "{\"walletAddress\":\"" + WALLET + "\",\"amount\":2,\"paymentAmount\":2,"
                + "\"paymentTransactionHash\":\"" + TX + "\"}";

        when(loyalty.mint(any(), any(LoyaltyMintCommand.class)))
                .thenReturn(LoyaltyMintResult.minted("0xmint"));
        mvc.perform(post("/rewards/api/mint-loyalty").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        when(loyalty.mint(any(), any(LoyaltyMintCommand.class)))
                .thenReturn(LoyaltyMintResult.ALREADY_CLAIMED);
        mvc.perform(post("/rewards/api/mint-loyalty").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body).session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Esta transacción ya ha sido reclamada."));

        when(loyalty.mint(any(), any(LoyaltyMintCommand.class)))
                .thenReturn(LoyaltyMintResult.mintFailed("boom"));
        mvc.perform(post("/rewards/api/mint-loyalty").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body).session(session))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/rewards/api/mint-loyalty").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"amount\":2", "\"amount\":0")).session(session))
                .andExpect(status().isBadRequest());
    }
}

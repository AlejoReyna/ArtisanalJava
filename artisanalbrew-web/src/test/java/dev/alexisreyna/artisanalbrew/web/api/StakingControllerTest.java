package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.CoffeeWeb3Service;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallengeService;
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

@WebMvcTest(StakingController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class StakingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CoffeeWeb3Service web3;
    @MockitoBean
    private StakingLedgerService ledger;
    @MockitoBean
    private BlockchainNetworkOptions chain;
    @MockitoBean
    private IdentityAccountService identityAccounts;
    @MockitoBean
    private EvmWalletChallengeService challenges;

    private static final String WALLET = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";

    @Test
    void balanceValidatesAndUnconfiguredStakingRefuses() throws Exception {
        when(web3.getCoffeeCoinBalance(WALLET)).thenReturn(new BigDecimal("12.5"));
        mvc.perform(get("/staking/api/coffee-balance").param("walletAddress", WALLET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("COFFEE"))
                .andExpect(jsonPath("$.walletAddress").value(WALLET));
        mvc.perform(get("/staking/api/coffee-balance").param("walletAddress", "nope"))
                .andExpect(status().isBadRequest());

        when(chain.effectivePaymentTokenContract()).thenReturn("");
        when(chain.getStakingPoolContract()).thenReturn("");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        mvc.perform(post("/staking/api/record-stake").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\",\"amount\":1,\"transactionHash\":\"0x"
                                + "ab".repeat(32) + "\"}")
                        .session(session))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/staking/clear-wallet-session").with(csrf()).session(session))
                .andExpect(status().is(302));
    }

    @Test
    void recordStakeHappyPath() throws Exception {
        when(chain.effectivePaymentTokenContract()).thenReturn("0x" + "11".repeat(20));
        when(chain.getStakingPoolContract()).thenReturn("0x" + "22".repeat(20));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        var entry = dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry
                .create("ethereum-sepolia", "0x" + "ab".repeat(32), 0, e -> {
                    e.setWalletAddress(WALLET);
                    e.setActionType("stake");
                    e.setAmount(BigDecimal.ONE);
                    e.setExplorerUrl("https://explorer.example/tx/0x");
                });
        when(ledger.record(any(), any(), any(), any(), any()))
                .thenReturn(StakingLedgerService.RecordResult.recorded(entry));
        mvc.perform(post("/staking/api/record-stake").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\",\"amount\":1,\"transactionHash\":\"0x"
                                + "ab".repeat(32) + "\"}")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        when(ledger.record(any(), any(), any(), any(), any()))
                .thenReturn(StakingLedgerService.RecordResult.ALREADY_RECORDED);
        mvc.perform(post("/staking/api/record-stake").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\",\"amount\":1,\"transactionHash\":\"0x"
                                + "ab".repeat(32) + "\"}")
                        .session(session))
                .andExpect(status().isConflict());
    }
}

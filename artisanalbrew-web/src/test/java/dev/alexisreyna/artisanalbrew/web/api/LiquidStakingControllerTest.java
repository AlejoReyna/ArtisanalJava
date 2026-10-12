package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingDashboard;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingGateway;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingLedgerService;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.LiquidStakingOperation;
import dev.alexisreyna.artisanalbrew.domain.entities.StakingLedgerEntry;
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
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LiquidStakingController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class LiquidStakingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LiquidStakingGateway gateway;
    @MockitoBean
    private LiquidStakingLedgerService ledger;
    @MockitoBean
    private ChainRegistry chains;
    @MockitoBean
    private SelectedChainAccessor selectedChain;

    private static final String WALLET = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";

    private static ChainDefinition chain() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setMinimumConfirmations(2);
        return chain;
    }

    @Test
    void dashboardRequiresSessionWallet() throws Exception {
        when(chains.tryGet("ethereum-sepolia")).thenReturn(Optional.of(chain()));
        mvc.perform(get("/staking/api/liquid/dashboard")
                        .param("chainKey", "ethereum-sepolia").param("walletIdentifier", WALLET))
                .andExpect(status().isUnauthorized());

        LiquidStakingDashboard dashboard = new LiquidStakingDashboard();
        dashboard.setConfigured(true);
        when(gateway.getDashboard("ethereum-sepolia", WALLET)).thenReturn(dashboard);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        mvc.perform(get("/staking/api/liquid/dashboard")
                        .param("chainKey", "ethereum-sepolia").param("walletIdentifier", WALLET)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true));
    }

    @Test
    void recordMapsOutcomes() throws Exception {
        when(chains.tryGet("ethereum-sepolia")).thenReturn(Optional.of(chain()));
        when(selectedChain.selectedChainKey()).thenReturn("ethereum-sepolia");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("WalletAddress", WALLET);
        String body = "{\"chainKey\":\"ethereum-sepolia\",\"walletIdentifier\":\"" + WALLET + "\","
                + "\"transactionId\":\"0x" + "ab".repeat(32) + "\"}";

        StakingLedgerEntry entry = StakingLedgerEntry.create("ethereum-sepolia", "0x" + "ab".repeat(32), 0, e -> {
            e.setWalletAddress(WALLET);
            e.setActionType("deposit");
            e.setAmount(BigDecimal.ONE);
            e.setExplorerUrl("");
        });
        when(ledger.record(any(), any(), any(), any(LiquidStakingOperation.class), any()))
                .thenReturn(LiquidStakingLedgerService.RecordResult.recorded(entry));
        mvc.perform(post("/staking/api/liquid/record-deposit").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        when(ledger.record(any(), any(), any(), any(LiquidStakingOperation.class), any()))
                .thenReturn(LiquidStakingLedgerService.RecordResult.PENDING_CONFIRMATIONS);
        mvc.perform(post("/staking/api/liquid/record-deposit").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body).session(session))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pending_confirmations"));

        mvc.perform(post("/staking/api/liquid/record-deposit").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chainKey\":\"ethereum-sepolia\",\"walletIdentifier\":\"nope\",\"transactionId\":\"0x00\"}")
                        .session(session))
                .andExpect(status().isBadRequest());
    }
}

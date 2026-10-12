package dev.alexisreyna.artisanalbrew.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.config.ChainRegistry;
import dev.alexisreyna.artisanalbrew.application.repository.UnitOfWork;
import dev.alexisreyna.artisanalbrew.application.repository.WalletStatusEventRepository;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccount;
import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.application.service.WalletAccountResult;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallenge;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallengeService;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallengeService;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletAuthController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class WalletAuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private EvmWalletChallengeService evmChallenges;
    @MockitoBean
    private SolanaWalletChallengeService solanaChallenges;
    @MockitoBean
    private ChainRegistry chains;
    @MockitoBean
    private SelectedChainAccessor selectedChain;
    @MockitoBean
    private BlockchainNetworkOptions legacyNetwork;
    @MockitoBean
    private UnitOfWork unitOfWork;
    @MockitoBean
    private SqsMessagePublisher publisher;
    @MockitoBean
    private WalletStatusEventRepository statusRepository;
    @MockitoBean
    private IdentityAccountService identityAccounts;

    private static final String WALLET = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";

    private static ChainDefinition evmChain() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("ethereum-sepolia");
        chain.setDisplayName("Ethereum Sepolia");
        chain.setFamily(ChainFamily.EVM);
        chain.setEnabled(true);
        chain.setEvmChainId(11155111);
        chain.setEvmChainIdHex("0xaa36a7");
        chain.setNativeCurrencyName("Sepolia ETH");
        chain.setNativeCurrencySymbol("ETH");
        chain.setPublicRpcUrl("https://rpc.example");
        chain.setExplorerAddressTemplate("https://explorer.example/address/{0}");
        chain.setCapabilities(new dev.alexisreyna.artisanalbrew.application.config.ChainCapabilities(
                true, false, false, false, false, false, false, false));
        return chain;
    }

    @Test
    void evmChallengeAndVerify() throws Exception {
        when(chains.tryGet("ethereum-sepolia")).thenReturn(Optional.of(evmChain()));
        when(evmChallenges.createChallenge(anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString()))
                .thenReturn(new EvmWalletChallenge("msg", "nonce", OffsetDateTime.now(), OffsetDateTime.now()));
        when(publisher.publish(any())).thenReturn(Optional.empty());

        mvc.perform(post("/api/wallet-auth/challenge").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"" + WALLET + "\",\"chainKey\":\"ethereum-sepolia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chainId").value(11155111))
                .andExpect(jsonPath("$.nonce").value("nonce"));

        mvc.perform(post("/api/wallet-auth/challenge").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"nope\",\"chainKey\":\"ethereum-sepolia\"}"))
                .andExpect(status().isBadRequest());

        when(evmChallenges.verify(anyString(), anyString(), anyString(), anyInt(), anyString(),
                anyString(), anyString()))
                .thenReturn(new EvmWalletChallengeService.Verification(
                        dev.alexisreyna.artisanalbrew.application.service.wallet.EvmChallengeVerificationError.NONE,
                        WALLET));
        when(identityAccounts.findOrCreateAndLinkWallet(anyString(), any(), any(), anyString()))
                .thenReturn(new WalletAccountResult(true,
                        new IdentityAccount("user-1", null, WALLET), false, null));
        mvc.perform(post("/api/wallet-auth/verify").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"" + WALLET + "\",\"signature\":\"0x00\",\"message\":\"msg\","
                                + "\"nonce\":\"nonce\",\"chainId\":11155111,\"chainKey\":\"ethereum-sepolia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.redirectUrl").value("/profile?tour=true"));

        mvc.perform(post("/api/wallet-auth/logout").with(csrf())).andExpect(status().is(302));
    }

    @Test
    void solanaChallengeAndStatus() throws Exception {
        ChainDefinition solana = evmChain();
        solana.setKey("solana-devnet");
        solana.setFamily(ChainFamily.SOLANA);
        solana.setSolanaCluster("devnet");
        when(chains.tryGet("solana-devnet")).thenReturn(Optional.of(solana));
        when(solanaChallenges.create(anyString(), any(), anyString()))
                .thenReturn(new dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallenge(
                        "msg", "n", OffsetDateTime.now(), OffsetDateTime.now()));
        mvc.perform(post("/api/wallet-auth/solana/challenge").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"SomePubkey\",\"chainKey\":\"solana-devnet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cluster").value("devnet"));

        when(statusRepository.getLatestForWallet(WALLET)).thenReturn(Optional.empty());
        mvc.perform(post("/api/wallet-auth/challenge").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"" + WALLET + "\",\"chainKey\":\"unknown-chain\"}"))
                .andExpect(status().isBadRequest());
    }
}

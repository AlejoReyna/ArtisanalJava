package dev.alexisreyna.artisanalbrew.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.application.repository.WalletStatusEventRepository;
import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletStatusEvent;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletStatusController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class WalletStatusControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private WalletStatusEventRepository repository;

    @MockitoBean
    private SqsMessagePublisher publisher;

    private static final String WALLET = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";

    @Test
    @WithMockUser
    void createStoresAndLooksUp() throws Exception {
        when(publisher.publish(any())).thenReturn(Optional.of("msg-1"));
        mvc.perform(post("/api/wallet-status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletAddress\":\"" + WALLET + "\",\"status\":\"online\","
                                + "\"payload\":{\"a\":1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedToAws").value(true))
                .andExpect(jsonPath("$.awsMessageId").value("msg-1"));

        WalletStatusEvent event = new WalletStatusEvent();
        event.setId(UUID.randomUUID());
        event.setWalletAddress(WALLET);
        event.setStatus("online");
        event.setCreatedAt(Instant.now());
        when(repository.getLatestForWallet(WALLET)).thenReturn(Optional.of(event));
        mvc.perform(get("/api/wallet-status/" + WALLET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));

        mvc.perform(get("/api/wallet-status/not-an-address")).andExpect(status().isBadRequest());
        when(repository.getLatestForWallet(WALLET)).thenReturn(Optional.empty());
        mvc.perform(get("/api/wallet-status/" + WALLET)).andExpect(status().isNotFound());
    }
}

package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.OrderDto;
import dev.alexisreyna.artisanalbrew.application.service.OrderService;
import dev.alexisreyna.artisanalbrew.application.service.ProfileService;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdersController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class OrdersControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orders;

    @MockitoBean
    private ProfileService profiles;

    private static final UUID PROFILE = UUID.randomUUID();

    private static org.springframework.security.core.Authentication auth() {
        return new UsernamePasswordAuthenticationToken("app-user-1", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void anonymousIs401() throws Exception {
        mvc.perform(post("/api/orders").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userProfileId\":\"" + PROFILE + "\",\"items\":[],\"walletAddress\":\"0xabc\","
                                + "\"paymentTransactionHash\":\"0x00\",\"paymentChainId\":1,\"paymentNetworkName\":\"n\","
                                + "\"paymentEthAmount\":1,\"paymentExplorerUrl\":\"e\",\"paidAtUtc\":\"2026-01-01T00:00:00Z\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orders/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void createDelegatesToProfileLinking() throws Exception {
        when(profiles.ensureProfileLinked("app-user-1")).thenReturn(PROFILE);
        when(orders.createOrder(any(), any())).thenAnswer(invocation ->
                new OrderDto(UUID.randomUUID(), "TC-1", PROFILE,
                        dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus.PROCESSING,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        null, null, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, "0xabc",
                        null, null, null, null, null, null, java.time.Instant.now(), List.of(), List.of()));
        mvc.perform(post("/api/orders").with(csrf())
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userProfileId\":\"" + PROFILE + "\",\"items\":[],\"walletAddress\":\"0xabc\","
                                + "\"paymentTransactionHash\":\"0x00\",\"paymentChainId\":1,\"paymentNetworkName\":\"n\","
                                + "\"paymentEthAmount\":1,\"paymentExplorerUrl\":\"e\",\"paidAtUtc\":\"2026-01-01T00:00:00Z\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void domainFailureMapsTo400() throws Exception {
        when(profiles.ensureProfileLinked("app-user-1")).thenReturn(PROFILE);
        when(orders.createOrder(any(), any())).thenThrow(new IllegalStateException("This payment has already been used for an order."));
        mvc.perform(post("/api/orders").with(csrf())
                        .with(authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userProfileId\":\"" + PROFILE + "\",\"items\":[],\"walletAddress\":\"0xabc\","
                                + "\"paymentTransactionHash\":\"0x00\",\"paymentChainId\":1,\"paymentNetworkName\":\"n\","
                                + "\"paymentEthAmount\":1,\"paymentExplorerUrl\":\"e\",\"paidAtUtc\":\"2026-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest());
    }
}

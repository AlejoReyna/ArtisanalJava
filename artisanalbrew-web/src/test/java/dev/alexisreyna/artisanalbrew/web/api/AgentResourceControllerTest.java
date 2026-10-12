package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentResourceController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
@TestPropertySource(properties = "agent-gateway.service-secret=topsecret")
class AgentResourceControllerTest {

    @Autowired
    private MockMvc mvc;

    private static final String BODY =
            "{\"productId\":\"house-espresso\",\"quantity\":2,\"correlationId\":\"corr-1\"}";

    @Test
    void rejectsWithoutSecretAndEchoesCorrelation() throws Exception {
        mvc.perform(post("/internal/agent/resources/wholesale-quote").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/internal/agent/resources/wholesale-quote").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-agent-gateway-secret", "wrong")
                        .content(BODY))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/internal/agent/resources/wholesale-quote").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-agent-gateway-secret", "topsecret")
                        .content("{\"productId\":\"house-espresso\",\"quantity\":2,"
                                + "\"correlationId\":\"corr-1\",\"requestHash\":\"" + "ab".repeat(32) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("x-correlation-id", "corr-1"))
                .andExpect(jsonPath("$.kind").value("wholesale-quote"))
                .andExpect(jsonPath("$.totalMinor").value(400000))
                .andExpect(jsonPath("$.currency").value("USDC"));
    }

    @Test
    void validatesPayloads() throws Exception {
        String secret = "topsecret";
        mvc.perform(post("/internal/agent/resources/wholesale-quote").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-agent-gateway-secret", secret)
                        .content("{\"productId\":\"x\",\"quantity\":0,\"correlationId\":\"c\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/internal/agent/resources/search-products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-agent-gateway-secret", secret)
                        .content("{\"query\":\"espresso\",\"correlationId\":\"c\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("product-search"));
        mvc.perform(post("/internal/agent/resources/brew-plan").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-agent-gateway-secret", secret)
                        .content("{\"productId\":\"x\",\"quantity\":1,\"correlationId\":\"" + "y".repeat(97) + "\"}"))
                .andExpect(status().isBadRequest());
    }
}

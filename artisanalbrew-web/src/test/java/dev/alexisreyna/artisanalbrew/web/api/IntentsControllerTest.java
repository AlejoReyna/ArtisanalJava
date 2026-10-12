package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteRequest;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteResult;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteService;
import dev.alexisreyna.artisanalbrew.application.service.SolverDenialReason;
import dev.alexisreyna.artisanalbrew.infrastructure.security.WebSecurityRules;
import dev.alexisreyna.artisanalbrew.web.support.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntentsController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class IntentsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IntentQuoteService quotes;

    @Test
    void validatesAndShapesQuotes() throws Exception {
        mvc.perform(get("/api/intents/quote")
                        .param("sourceToken", "").param("destinationToken", "0xbbb").param("amountIn", "100"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/intents/quote")
                        .param("sourceToken", "0xaaa").param("destinationToken", "0xbbb").param("amountIn", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/intents/quote")
                        .param("sourceToken", "0xaaa").param("destinationToken", "0xbbb").param("amountIn", "abc"))
                .andExpect(status().isBadRequest());

        when(quotes.getQuote(any(IntentQuoteRequest.class)))
                .thenReturn(IntentQuoteResult.quotable(new BigInteger("99"), "ethereum-sepolia", "base-sepolia"));
        mvc.perform(get("/api/intents/quote")
                        .param("sourceToken", "0xaaa").param("destinationToken", "0xbbb").param("amountIn", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fillable").value(true))
                .andExpect(jsonPath("$.amountOut").value("99"));

        when(quotes.getQuote(any(IntentQuoteRequest.class)))
                .thenReturn(IntentQuoteResult.notQuotable(SolverDenialReason.DISALLOWED_TOKEN_PAIR, "nope"));
        mvc.perform(get("/api/intents/quote")
                        .param("sourceToken", "0xaaa").param("destinationToken", "0xzzz").param("amountIn", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fillable").value(false))
                .andExpect(jsonPath("$.reason").value("DISALLOWED_TOKEN_PAIR"));
    }
}

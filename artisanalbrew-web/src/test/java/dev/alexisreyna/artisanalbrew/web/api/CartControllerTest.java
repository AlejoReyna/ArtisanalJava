package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.web.cart.CartLine;
import dev.alexisreyna.artisanalbrew.web.cart.SessionCartService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import({GlobalExceptionHandler.class, WebSecurityRules.class})
class CartControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SessionCartService carts;

    private static List<CartLine> lines() {
        return List.of(new CartLine("espresso", "Espresso", "image-frame--coldbrew",
                new BigDecimal("10.00"), 2, null, UUID.randomUUID()));
    }

    @Test
    void addRequiresSlugAndReturnsCounts() throws Exception {
        mvc.perform(post("/api/cart/items").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"\",\"quantity\":1}"))
                .andExpect(status().isBadRequest());

        when(carts.add(any(), anyString(), anyInt())).thenReturn(lines());
        mvc.perform(post("/api/cart/items").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"espresso\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.lines[0].lineTotal").value(20.00));

        when(carts.add(any(), anyString(), anyInt()))
                .thenThrow(new IllegalStateException("Product 'x' was not found in the catalog."));
        mvc.perform(post("/api/cart/items").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"x\",\"quantity\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setRemoveClear() throws Exception {
        when(carts.setQuantity(any(), anyString(), anyInt())).thenReturn(lines());
        mvc.perform(put("/api/cart/items/espresso").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2));

        when(carts.remove(any(), anyString())).thenReturn(List.of());
        mvc.perform(delete("/api/cart/items/espresso").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));

        when(carts.clear(any())).thenReturn(List.of());
        mvc.perform(delete("/api/cart").with(csrf())).andExpect(status().isOk());
    }
}

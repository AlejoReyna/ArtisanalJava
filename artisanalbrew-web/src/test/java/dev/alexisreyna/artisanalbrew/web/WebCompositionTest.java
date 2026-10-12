package dev.alexisreyna.artisanalbrew.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full composition proof: real services, migrated H2, real filter chain.
 * Anonymous API access answers 401 (not a login redirect), CSRF is enforced
 * off the cart path, and role rules hold end to end.
 */
@SpringBootTest(classes = ArtisanalBrewApplication.class)
@AutoConfigureMockMvc
class WebCompositionTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void publicSurfaceIsOpen() throws Exception {
        mvc.perform(get("/api/chains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultChainKey").value("ethereum-sepolia"))
                .andExpect(jsonPath("$.chains[?(@.key == 'ethereum-sepolia')].deployments.cafe")
                        .value("0x15DbED39271D2788a9Be63ffB34C8E2DdED8754A"));
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/transparency/stats")).andExpect(status().isOk());
    }

    @Test
    void anonymousApiAccessIs401AndCsrfEnforced() throws Exception {
        mvc.perform(get("/api/profile/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orders/me")).andExpect(status().isUnauthorized());

        // No CSRF token and no auth: rejected before any business logic runs.
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"description\":\"x\",\"price\":1,\"stockQuantity\":1,\"category\":0}"))
                .andExpect(status().isForbidden());
        // Cart is the Csrf-ignored storefront path: passes CSRF, fails auth-free.
        mvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"house-espresso\",\"quantity\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void adminWritesWhileUsersAreRefused() throws Exception {
        String body = "{\"name\":\"Test Brew\",\"description\":\"Desc\",\"price\":9.5,"
                + "\"stockQuantity\":5,\"category\":0}";
        mvc.perform(post("/api/products").with(csrf())
                        .with(user("someone").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/products").with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("test-brew"));

        mvc.perform(get("/api/products/slug/test-brew")).andExpect(status().isOk());
    }
}

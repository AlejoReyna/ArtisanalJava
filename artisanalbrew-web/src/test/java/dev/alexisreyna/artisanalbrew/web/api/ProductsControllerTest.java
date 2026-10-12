package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.ProductDto;
import dev.alexisreyna.artisanalbrew.application.service.ProductService;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.web.support.DotNetEnumJacksonModule;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductsController.class)
@Import({GlobalExceptionHandler.class, DotNetEnumJacksonModule.class, WebSecurityRules.class})
class ProductsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProductService products;

    private static ProductDto product() {
        return new ProductDto(UUID.fromString("11111111-1111-1111-1111-111111111111"), "House Espresso",
                "house-espresso", "Bright", new BigDecimal("3.50"), 100, null, ProductCategory.BEANS, true,
                "Brazil", dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel.DARK,
                dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess.NATURAL);
    }

    @Test
    void listAndGet() throws Exception {
        when(products.getProducts()).thenReturn(List.of(product()));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("house-espresso"))
                .andExpect(jsonPath("$[0].category").value(0))
                .andExpect(jsonPath("$[0].roastLevel").value(2));

        when(products.getBySlug("house-espresso")).thenReturn(Optional.of(product()));
        mvc.perform(get("/api/products/slug/house-espresso")).andExpect(status().isOk());
        when(products.getById(any())).thenReturn(Optional.empty());
        mvc.perform(get("/api/products/11111111-1111-1111-1111-111111111112"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WithLocation() throws Exception {
        when(products.create(any())).thenReturn(product());
        mvc.perform(post("/api/products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"House Espresso\",\"description\":\"Bright\",\"price\":3.50,"
                                + "\"stockQuantity\":100,\"category\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void serviceValidationMapsTo400() throws Exception {
        when(products.create(any())).thenThrow(
                new dev.alexisreyna.artisanalbrew.application.validation.ValidationException("Name must be 1-160 characters."));
        mvc.perform(post("/api/products").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"x\",\"price\":1,\"stockQuantity\":1,\"category\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Name must be 1-160 characters."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMapsTo204Or404() throws Exception {
        when(products.delete(any())).thenReturn(true);
        mvc.perform(delete("/api/products/11111111-1111-1111-1111-111111111111").with(csrf()))
                .andExpect(status().isNoContent());
        when(products.delete(any())).thenReturn(false);
        mvc.perform(delete("/api/products/11111111-1111-1111-1111-111111111111").with(csrf()))
                .andExpect(status().isNotFound());
    }
}

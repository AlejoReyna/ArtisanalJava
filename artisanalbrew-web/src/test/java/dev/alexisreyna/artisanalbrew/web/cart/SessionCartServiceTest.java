package dev.alexisreyna.artisanalbrew.web.cart;

import dev.alexisreyna.artisanalbrew.application.dto.ProductDto;
import dev.alexisreyna.artisanalbrew.application.service.ProductService;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionCartServiceTest {

    private static ProductDto product(String slug, BigDecimal price, int stock, boolean active) {
        return new ProductDto(UUID.randomUUID(), "Espresso", slug, "Desc", price, stock, null,
                ProductCategory.BEANS, active, null, null, null);
    }

    @Test
    void addMergesSetRemovesAndClears() {
        ProductService products = mock(ProductService.class);
        when(products.getBySlug("espresso"))
                .thenReturn(Optional.of(product("espresso", new BigDecimal("10.00"), 10, true)));
        SessionCartService carts = new SessionCartService(products);
        MockHttpSession session = new MockHttpSession();

        var lines = carts.add(session, "espresso", 2);
        assertThat(lines).hasSize(1);
        assertThat(carts.subtotal(lines)).isEqualByComparingTo("20.00");
        lines = carts.add(session, "espresso", 1);
        assertThat(lines.get(0).quantity()).isEqualTo(3);
        lines = carts.setQuantity(session, "espresso", 1);
        assertThat(lines.get(0).quantity()).isEqualTo(1);
        lines = carts.setQuantity(session, "espresso", 0);
        assertThat(lines).isEmpty();
        carts.add(session, "espresso", 1);
        assertThat(carts.remove(session, "espresso")).isEmpty();
        carts.add(session, "espresso", 1);
        assertThat(carts.clear(session)).isEmpty();
        assertThat(carts.toOrderItems(carts.add(session, "espresso", 2)).get(0).unitPrice())
                .isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsBadInput() {
        ProductService products = mock(ProductService.class);
        when(products.getBySlug("gone")).thenReturn(Optional.empty());
        when(products.getBySlug("off")).thenReturn(Optional.of(product("off", BigDecimal.ONE, 5, false)));
        when(products.getBySlug("low")).thenReturn(Optional.of(product("low", BigDecimal.ONE, 1, true)));
        SessionCartService carts = new SessionCartService(products);
        MockHttpSession session = new MockHttpSession();
        assertThatThrownBy(() -> carts.add(session, "espresso", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carts.add(session, "gone", 1)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> carts.add(session, "off", 1)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> carts.add(session, "low", 5)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only 1 unit");
        assertThat(carts.setQuantity(session, "missing", 3)).isEmpty();
        assertThat(SessionCartService.imageClassFor(ProductCategory.CERAMICS_AND_GOODS))
                .isEqualTo("image-frame--ceramics");
        assertThat(SessionCartService.imageClassFor(ProductCategory.BEANS)).isEqualTo("image-frame--coldbrew");
    }
}

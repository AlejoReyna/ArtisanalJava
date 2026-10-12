package dev.alexisreyna.artisanalbrew.web.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port of {@code MarketplaceCartLine} ({@code Web/Services/Cart}).
 * The Blazor circuit-snapshot plumbing has no REST equivalent and is gone;
 * the line shape (and {@code lineTotal}) is preserved for the cart API.
 */
public record CartLine(
        String slug,
        String name,
        String imageClass,
        BigDecimal unitPrice,
        int quantity,
        String imageUrl,
        UUID productId) implements Serializable {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Jackson only sees record components: expose the computed total explicitly. */
    @com.fasterxml.jackson.annotation.JsonProperty("lineTotal")
    public BigDecimal serializedLineTotal() {
        return lineTotal();
    }
}

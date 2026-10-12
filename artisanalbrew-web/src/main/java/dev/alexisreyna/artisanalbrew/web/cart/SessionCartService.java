package dev.alexisreyna.artisanalbrew.web.cart;

import dev.alexisreyna.artisanalbrew.application.dto.CartItemDto;
import dev.alexisreyna.artisanalbrew.application.service.ProductService;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Session-backed cart (mirrors {@code ShoppingCartService} session behavior:
 * catalog requote on add, merge on re-add, remove-on-zero). The Blazor
 * circuit cache/cookie persistence has no REST equivalent.
 */
@Service
public class SessionCartService {
    private static final String SESSION_KEY = "ArtisanalBrew.Cart";

    private final ProductService products;

    public SessionCartService(ProductService products) {
        this.products = products;
    }

    @SuppressWarnings("unchecked")
    public List<CartLine> lines(HttpSession session) {
        Object stored = session.getAttribute(SESSION_KEY);
        if (stored instanceof List<?> list) {
            return new ArrayList<>((List<CartLine>) list);
        }
        return new ArrayList<>();
    }

    public List<CartLine> add(HttpSession session, String slug, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        var product = products.getBySlug(slug)
                .orElseThrow(() -> new IllegalStateException("Product '" + slug + "' was not found in the catalog."));
        if (!product.isActive()) {
            throw new IllegalStateException("Product '" + slug + "' is not available.");
        }
        if (product.stockQuantity() < quantity) {
            throw new IllegalStateException("Only " + product.stockQuantity()
                    + " unit(s) of '" + product.name() + "' are available.");
        }
        List<CartLine> lines = lines(session);
        int index = indexOf(lines, slug);
        if (index >= 0) {
            CartLine current = lines.get(index);
            lines.set(index, withQuantity(current, current.quantity() + quantity));
        } else {
            lines.add(new CartLine(product.slug(), product.name(), imageClassFor(product.category()),
                    product.price(), quantity, product.imageUrl(), product.id()));
        }
        session.setAttribute(SESSION_KEY, new ArrayList<>(lines));
        return lines;
    }

    public List<CartLine> setQuantity(HttpSession session, String slug, int quantity) {
        List<CartLine> lines = lines(session);
        int index = indexOf(lines, slug);
        if (index < 0) {
            return lines;
        }
        if (quantity <= 0) {
            lines.remove(index);
        } else {
            lines.set(index, withQuantity(lines.get(index), quantity));
        }
        session.setAttribute(SESSION_KEY, new ArrayList<>(lines));
        return lines;
    }

    public List<CartLine> remove(HttpSession session, String slug) {
        List<CartLine> lines = lines(session);
        lines.removeIf(line -> line.slug().equals(slug));
        session.setAttribute(SESSION_KEY, new ArrayList<>(lines));
        return lines;
    }

    public List<CartLine> clear(HttpSession session) {
        session.setAttribute(SESSION_KEY, new ArrayList<CartLine>());
        return List.of();
    }

    public BigDecimal subtotal(List<CartLine> lines) {
        return lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CartItemDto> toOrderItems(List<CartLine> lines) {
        return lines.stream()
                .map(line -> new CartItemDto(line.productId(), line.name(), line.quantity(), line.unitPrice()))
                .toList();
    }

    private static int indexOf(List<CartLine> lines, String slug) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).slug().equals(slug)) {
                return i;
            }
        }
        return -1;
    }

    private static CartLine withQuantity(CartLine line, int quantity) {
        return new CartLine(line.slug(), line.name(), line.imageClass(), line.unitPrice(),
                quantity, line.imageUrl(), line.productId());
    }

    static String imageClassFor(ProductCategory category) {
        if (category == ProductCategory.BREWING_EQUIPMENT) {
            return "image-frame--equipment";
        }
        if (category == ProductCategory.CERAMICS_AND_GOODS) {
            return "image-frame--ceramics";
        }
        return "image-frame--coldbrew";
    }
}

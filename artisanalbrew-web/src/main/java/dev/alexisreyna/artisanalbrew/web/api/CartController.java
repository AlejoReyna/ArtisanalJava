package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.web.cart.CartLine;
import dev.alexisreyna.artisanalbrew.web.cart.SessionCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Port of {@code CartController} ({@code Web/Controllers}).
 * Antiforgery is ignored here exactly like {@code [IgnoreAntiforgeryToken]} —
 * the cart API is called cross-component by public JS without a token.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final SessionCartService carts;

    public CartController(SessionCartService carts) {
        this.carts = carts;
    }

    @PostMapping("/items")
    public ResponseEntity<?> add(@RequestBody AddItemRequest request, HttpSession session) {
        if (request.slug() == null || request.slug().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product slug is required."));
        }
        try {
            List<CartLine> lines = carts.add(session, request.slug(), request.quantity());
            return ResponseEntity.ok(response(lines));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/items/{slug}")
    public ResponseEntity<?> setQuantity(@PathVariable String slug,
                                         @RequestBody SetQuantityRequest request, HttpSession session) {
        List<CartLine> lines = carts.setQuantity(session, slug, request.quantity());
        return ResponseEntity.ok(response(lines));
    }

    @DeleteMapping("/items/{slug}")
    public ResponseEntity<?> remove(@PathVariable String slug, HttpSession session) {
        return ResponseEntity.ok(response(carts.remove(session, slug)));
    }

    @DeleteMapping
    public ResponseEntity<?> clear(HttpSession session) {
        return ResponseEntity.ok(response(carts.clear(session)));
    }

    private Map<String, Object> response(List<CartLine> lines) {
        int itemCount = lines.stream().mapToInt(CartLine::quantity).sum();
        return Map.of("itemCount", itemCount, "lines", lines);
    }

    public record AddItemRequest(String slug, int quantity) {
        public AddItemRequest {
            if (quantity == 0) {
                quantity = 1;
            }
        }
    }

    public record SetQuantityRequest(int quantity) {
    }
}

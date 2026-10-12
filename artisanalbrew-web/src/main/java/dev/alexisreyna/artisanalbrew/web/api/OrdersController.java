package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.dto.OrderDto;
import dev.alexisreyna.artisanalbrew.application.service.OrderService;
import dev.alexisreyna.artisanalbrew.application.service.ProfileService;
import dev.alexisreyna.artisanalbrew.application.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Port of {@code OrdersController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderService orders;
    private final ProfileService profiles;

    public OrdersController(OrderService orders, ProfileService profiles) {
        this.orders = orders;
        this.profiles = profiles;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest request, Authentication authentication) {
        UUID profileId = requireProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            OrderDto order = orders.createOrder(request, profileId);
            return ResponseEntity.created(URI.create("/api/orders/me")).body(order);
        } catch (ValidationException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> mine(Authentication authentication) {
        UUID profileId = requireProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        List<OrderDto> mine = orders.getOrdersForUser(profileId);
        return ResponseEntity.ok(mine);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return orders.deleteOrder(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private UUID requireProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        return profiles.ensureProfileLinked(authentication.getName());
    }
}

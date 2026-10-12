package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.OrderDto;
import dev.alexisreyna.artisanalbrew.application.dto.ProfileDashboardDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UserProfileDto;
import dev.alexisreyna.artisanalbrew.application.service.OrderService;
import dev.alexisreyna.artisanalbrew.application.service.ProfileService;
import dev.alexisreyna.artisanalbrew.application.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Port of {@code ProfileController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profiles;
    private final OrderService orders;

    public ProfileController(ProfileService profiles, OrderService orders) {
        this.profiles = profiles;
        this.orders = orders;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        UUID profileId = linkedProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        ProfileDashboardDto dashboard = profiles.getDashboard(profileId);
        return ResponseEntity.ok(dashboard);
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody UpdateUserProfileRequest request, Authentication authentication) {
        UUID profileId = linkedProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserProfileDto dto = profiles.updateDisplayName(profileId, request);
            return ResponseEntity.ok(dto);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", Map.of("displayName", List.of(e.getMessage()))));
        }
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(@RequestBody UpdateAvatarRequest request, Authentication authentication) {
        UUID profileId = linkedProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            return ResponseEntity.ok(profiles.updateAvatar(profileId, request));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", Map.of("avatar", List.of(e.getMessage()))));
        }
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<?> resetAvatar(Authentication authentication) {
        UUID profileId = linkedProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(profiles.resetAvatar(profileId));
    }

    @GetMapping("/me/orders")
    public ResponseEntity<?> myOrders(Authentication authentication) {
        UUID profileId = linkedProfile(authentication);
        if (profileId == null) {
            return ResponseEntity.status(401).build();
        }
        List<OrderDto> mine = orders.getOrdersForUser(profileId);
        return ResponseEntity.ok(mine);
    }

    private UUID linkedProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }
        return profiles.ensureProfileLinked(authentication.getName());
    }
}

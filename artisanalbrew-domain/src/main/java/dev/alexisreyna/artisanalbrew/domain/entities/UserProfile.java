package dev.alexisreyna.artisanalbrew.domain.entities;

import dev.alexisreyna.artisanalbrew.domain.avatars.RobotAvatar;
import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Port of {@code ThisCafeteria.Domain/Entities/UserProfile.cs}.
 *
 * <p>{@code avatar == null} is the meaningful "never chosen" state: the look then comes
 * from {@code AvatarSeed.fromWallet} instead of this field. Do not default it.</p>
 */
public class UserProfile {
    private UUID id = UUID.randomUUID();
    private String email = "";
    private String displayName = "";
    private UserRole role = UserRole.CUSTOMER;
    private Instant createdAt = Instant.now();
    private RobotAvatar avatar;
    private List<Order> orders = new ArrayList<>();
    private List<CouponRedemption> couponRedemptions = new ArrayList<>();
    private Cart cart;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public RobotAvatar getAvatar() { return avatar; }
    public void setAvatar(RobotAvatar avatar) { this.avatar = avatar; }
    public List<Order> getOrders() { return orders; }
    public List<CouponRedemption> getCouponRedemptions() { return couponRedemptions; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}

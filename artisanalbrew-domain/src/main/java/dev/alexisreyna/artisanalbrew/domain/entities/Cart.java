package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/Cart.cs}. */
public class Cart {
    private UUID id = UUID.randomUUID();
    private UUID userProfileId;
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
    private List<CartItem> items = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserProfileId() { return userProfileId; }
    public void setUserProfileId(UUID userProfileId) { this.userProfileId = userProfileId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}

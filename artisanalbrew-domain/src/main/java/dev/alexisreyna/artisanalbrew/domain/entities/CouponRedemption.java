package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/CouponRedemption.cs}. */
public class CouponRedemption {
    private UUID id = UUID.randomUUID();
    private UUID couponId;
    private UUID userProfileId;
    private UUID orderId;
    private Instant redeemedAtUtc = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCouponId() { return couponId; }
    public void setCouponId(UUID couponId) { this.couponId = couponId; }
    public UUID getUserProfileId() { return userProfileId; }
    public void setUserProfileId(UUID userProfileId) { this.userProfileId = userProfileId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public Instant getRedeemedAtUtc() { return redeemedAtUtc; }
    public void setRedeemedAtUtc(Instant redeemedAtUtc) { this.redeemedAtUtc = redeemedAtUtc; }
}

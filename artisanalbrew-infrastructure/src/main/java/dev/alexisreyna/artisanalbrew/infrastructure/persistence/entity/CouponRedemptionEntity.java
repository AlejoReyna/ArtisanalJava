package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code CouponRedemptions}. */
@Entity
@Table(name = "\"CouponRedemptions\"")
public class CouponRedemptionEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"CouponId\"")
    public UUID couponId;

    @Column(name = "\"UserProfileId\"")
    public UUID userProfileId;

    @Column(name = "\"OrderId\"")
    public UUID orderId;

    @Column(name = "\"RedeemedAtUtc\"")
    public Instant redeemedAtUtc;
}

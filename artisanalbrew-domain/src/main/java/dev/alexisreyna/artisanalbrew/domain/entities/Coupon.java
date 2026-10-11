package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/Coupon.cs}. */
public class Coupon {
    private UUID id = UUID.randomUUID();
    private String code = "";
    private String normalizedCode = "";
    private BigDecimal discountPercent;
    private BigDecimal minimumOrderTotal = BigDecimal.ZERO;
    private boolean active = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    public static Coupon create(String code, BigDecimal discountPercent, BigDecimal minimumOrderTotal) {
        return create(code, discountPercent, minimumOrderTotal, null);
    }

    public static Coupon create(String code, BigDecimal discountPercent, BigDecimal minimumOrderTotal, Instant createdAtUtc) {
        Coupon coupon = new Coupon();
        coupon.createdAt = createdAtUtc != null ? createdAtUtc : Instant.now();
        coupon.update(code, discountPercent, minimumOrderTotal, true, null);
        return coupon;
    }

    public void update(String code, BigDecimal discountPercent, BigDecimal minimumOrderTotal, boolean isActive, Instant updatedAtUtc) {
        if (code == null || code.isBlank()
                || discountPercent == null || discountPercent.signum() <= 0
                || discountPercent.compareTo(new BigDecimal("100")) > 0
                || minimumOrderTotal == null || minimumOrderTotal.signum() < 0) {
            throw new IllegalStateException("Coupon terms are invalid.");
        }
        this.code = code.trim();
        this.normalizedCode = normalizeCode(code);
        this.discountPercent = discountPercent;
        this.minimumOrderTotal = minimumOrderTotal;
        this.active = isActive;
        this.updatedAt = updatedAtUtc;
    }

    public boolean canBeRedeemedFor(BigDecimal orderTotal) {
        return active && orderTotal != null && orderTotal.compareTo(minimumOrderTotal) >= 0;
    }

    public void deactivate() {
        deactivate(null);
    }

    public void deactivate(Instant updatedAtUtc) {
        this.active = false;
        this.updatedAt = updatedAtUtc != null ? updatedAtUtc : Instant.now();
    }

    public static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getNormalizedCode() { return normalizedCode; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public BigDecimal getMinimumOrderTotal() { return minimumOrderTotal; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

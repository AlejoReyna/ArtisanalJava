package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;

/** Port of {@code SponsorshipDecision} in {@code Services/ISponsorshipPolicyService.cs}. */
public record SponsorshipDecision(
        boolean approved,
        SponsorshipDenialReason reason,
        String detail,
        BigDecimal remainingUsd) {

    public static SponsorshipDecision approve(BigDecimal remainingUsd) {
        return new SponsorshipDecision(true, SponsorshipDenialReason.NONE, "", remainingUsd);
    }

    public static SponsorshipDecision deny(SponsorshipDenialReason reason, String detail) {
        return new SponsorshipDecision(false, reason, detail, BigDecimal.ZERO);
    }
}

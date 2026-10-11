package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;

/** Port of {@code SponsorshipRequest} in {@code Services/ISponsorshipPolicyService.cs}. */
public record SponsorshipRequest(
        String chainKey,
        String ownerAddress,
        BigDecimal estimatedCostUsd,
        String targetAddress,
        String selector) {
}

package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;

/** Port of {@code SponsorshipSignature} in {@code Services/IUserOperationSponsor.cs}. */
public record SponsorshipSignature(
        boolean approved,
        SponsorshipDenialReason reason,
        String detail,
        String paymasterAndData,
        BigDecimal costUsd) {

    public static SponsorshipSignature deny(SponsorshipDenialReason reason, String detail) {
        return new SponsorshipSignature(false, reason, detail, "", BigDecimal.ZERO);
    }
}

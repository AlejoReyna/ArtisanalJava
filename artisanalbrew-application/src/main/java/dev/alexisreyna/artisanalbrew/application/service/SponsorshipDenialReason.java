package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;

/** Port of {@code SponsorshipDenialReason} in {@code Services/ISponsorshipPolicyService.cs}. */
public enum SponsorshipDenialReason {
    NONE,
    NOT_CONFIGURED,
    NO_GRANT,
    REVOKED,
    EXPIRED,
    NOT_YET_VALID,
    OVER_BUDGET,
    OPERATION_TOO_EXPENSIVE,
    DISALLOWED_TARGET,
    DISALLOWED_SELECTOR,
    INVALID_REQUEST,
    SIMULATION_FAILED
}

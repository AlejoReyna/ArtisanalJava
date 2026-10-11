package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code SolverDenialReason} in {@code Services/ISolverPolicyService.cs}. */
public enum SolverDenialReason {
    NONE,
    NOT_CONFIGURED,
    DISALLOWED_TOKEN_PAIR,
    AMOUNT_TOO_LARGE,
    EXPIRED,
    NOT_ALLOWED_SOLVER,
    OUTPUT_BELOW_MINIMUM,
    INVALID_REQUEST
}

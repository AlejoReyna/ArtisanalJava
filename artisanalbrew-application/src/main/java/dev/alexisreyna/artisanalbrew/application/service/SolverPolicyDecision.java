package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code SolverPolicyDecision} in {@code Services/ISolverPolicyService.cs}. */
public record SolverPolicyDecision(
        boolean approved,
        SolverDenialReason reason,
        String detail,
        BigInteger amountOut) {

    public static SolverPolicyDecision approve(BigInteger amountOut) {
        return new SolverPolicyDecision(true, SolverDenialReason.NONE, "", amountOut);
    }

    public static SolverPolicyDecision deny(SolverDenialReason reason, String detail) {
        return new SolverPolicyDecision(false, reason, detail, BigInteger.ZERO);
    }
}

package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code UserOperationSimulationResult} in {@code Services/IUserOperationSimulator.cs}. */
public record UserOperationSimulationResult(
        boolean success,
        String failureReason,
        BigInteger preOpGas,
        BigInteger paidWei) {

    public static UserOperationSimulationResult failure(String reason) {
        return new UserOperationSimulationResult(false, reason, BigInteger.ZERO, BigInteger.ZERO);
    }
}

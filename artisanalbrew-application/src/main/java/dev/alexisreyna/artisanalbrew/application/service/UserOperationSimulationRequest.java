package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code UserOperationSimulationRequest} in {@code Services/IUserOperationSimulator.cs}. */
public record UserOperationSimulationRequest(
        String chainKey,
        String sender,
        BigInteger nonce,
        String initCode,
        String callData,
        String accountGasLimits,
        BigInteger preVerificationGas,
        String gasFees) {
}

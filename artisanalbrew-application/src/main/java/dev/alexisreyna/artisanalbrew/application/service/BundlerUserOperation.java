package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code BundlerUserOperation} in {@code Services/IBundlerClient.cs}. */
public record BundlerUserOperation(
        String sender,
        BigInteger nonce,
        String initCode,
        String callData,
        String accountGasLimits,
        BigInteger preVerificationGas,
        String gasFees,
        String paymasterAndData,
        String signature) {
}

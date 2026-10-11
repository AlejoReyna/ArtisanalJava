package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code BundlerGasEstimate} in {@code Services/IBundlerClient.cs}. */
public record BundlerGasEstimate(
        BigInteger preVerificationGas,
        BigInteger verificationGasLimit,
        BigInteger callGasLimit,
        BigInteger paymasterVerificationGasLimit,
        BigInteger paymasterPostOpGasLimit) {
}

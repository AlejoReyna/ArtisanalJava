package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Port of {@code SponsoredUserOperation} in {@code Services/IUserOperationSponsor.cs}.
 * Target + selector are required by shape: no paymaster signature without them.
 * No cost field: cost is derived from simulation, never caller-supplied.
 */
public record SponsoredUserOperation(
        String chainKey,
        String ownerAddress,
        String sender,
        BigInteger nonce,
        String initCode,
        String callData,
        String accountGasLimits,
        BigInteger preVerificationGas,
        String gasFees,
        String targetAddress,
        String selector) {
}

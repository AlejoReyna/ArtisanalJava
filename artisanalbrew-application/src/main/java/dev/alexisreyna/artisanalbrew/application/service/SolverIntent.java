package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code SolverIntent} in {@code Services/ISolverPolicyService.cs}. */
public record SolverIntent(
        String orderId,
        String user,
        String sourceToken,
        BigInteger amountIn,
        BigInteger destinationChainId,
        String destinationToken,
        String destinationReceiver,
        BigInteger minAmountOut,
        BigInteger deadline,
        BigInteger nonce,
        String allowedSolver,
        String submitTransactionHash) {
}

package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code BundlerReceipt} in {@code Services/IBundlerClient.cs}. */
public record BundlerReceipt(
        String userOperationHash,
        String transactionHash,
        String sender,
        BigInteger nonce,
        boolean success,
        String revertReason) {
}

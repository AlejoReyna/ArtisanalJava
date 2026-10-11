package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code IntentQuoteResult} in {@code Services/IIntentQuoteService.cs}. */
public record IntentQuoteResult(
        boolean fillable,
        BigInteger amountOut,
        SolverDenialReason denialReason,
        String detail,
        String sourceChainKey,
        String destinationChainKey) {

    public static IntentQuoteResult quotable(BigInteger amountOut, String sourceChainKey, String destinationChainKey) {
        return new IntentQuoteResult(true, amountOut, null, "", sourceChainKey, destinationChainKey);
    }

    public static IntentQuoteResult notQuotable(SolverDenialReason reason, String detail) {
        return new IntentQuoteResult(false, BigInteger.ZERO, reason, detail, "", "");
    }
}

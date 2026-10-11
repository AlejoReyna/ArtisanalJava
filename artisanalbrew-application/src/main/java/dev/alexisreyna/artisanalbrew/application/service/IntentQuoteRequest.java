package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigInteger;

/** Port of {@code IntentQuoteRequest} in {@code Services/IIntentQuoteService.cs}. */
public record IntentQuoteRequest(String sourceToken, String destinationToken, BigInteger amountIn) {
}

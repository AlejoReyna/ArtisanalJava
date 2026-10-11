package dev.alexisreyna.artisanalbrew.application.service;

/**
 * Port of {@code IIntentQuoteService} in {@code Services/IIntentQuoteService.cs}.
 * Must delegate to the same {@link SolverPolicyService} the worker uses — never
 * reimplement pricing, or quote and outcome can disagree.
 */
public interface IntentQuoteService {
    IntentQuoteResult getQuote(IntentQuoteRequest request);
}

package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteRequest;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteResult;
import dev.alexisreyna.artisanalbrew.application.service.IntentQuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;

/** Port of {@code IntentsController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/api/intents")
public class IntentsController {
    private final IntentQuoteService quotes;

    public IntentsController(IntentQuoteService quotes) {
        this.quotes = quotes;
    }

    @GetMapping("/quote")
    public ResponseEntity<?> quote(@RequestParam String sourceToken,
                                   @RequestParam String destinationToken,
                                   @RequestParam String amountIn) {
        if (sourceToken.isBlank() || destinationToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "sourceToken and destinationToken are required."));
        }
        BigInteger parsed;
        try {
            parsed = new BigInteger(amountIn);
        } catch (NumberFormatException e) {
            parsed = null;
        }
        if (parsed == null || parsed.signum() <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "amountIn must be a positive integer, in the source token's base units."));
        }
        IntentQuoteResult quote = quotes.getQuote(new IntentQuoteRequest(sourceToken, destinationToken, parsed));
        if (!quote.fillable()) {
            return ResponseEntity.ok(Map.of(
                    "fillable", false,
                    "reason", String.valueOf(quote.denialReason()),
                    "detail", quote.detail()));
        }
        return ResponseEntity.ok(Map.of(
                "fillable", true,
                "amountOut", quote.amountOut().toString(),
                "sourceChainKey", quote.sourceChainKey(),
                "destinationChainKey", quote.destinationChainKey()));
    }
}

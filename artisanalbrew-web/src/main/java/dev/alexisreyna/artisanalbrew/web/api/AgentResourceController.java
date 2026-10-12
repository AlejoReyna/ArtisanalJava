package dev.alexisreyna.artisanalbrew.web.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Port of {@code AgentResourceController} ({@code Web/Controllers}):
 * deterministic paid-agent stubs behind a constant-time bearer secret.
 */
@RestController
@RequestMapping("/internal/agent/resources")
public class AgentResourceController {
    private final String serviceSecret;

    public AgentResourceController(@Value("${agent-gateway.service-secret:}") String serviceSecret) {
        this.serviceSecret = serviceSecret == null ? "" : serviceSecret;
    }

    @PostMapping("/search-products")
    public ResponseEntity<?> searchProducts(@RequestBody SearchProductsRequest request,
                                            @RequestHeader(value = "x-agent-gateway-secret", required = false) String secret) {
        if (request.query() == null || request.query().isBlank() || request.query().length() > 200) {
            return ResponseEntity.badRequest().build();
        }
        return authorized(request, secret, () -> ResponseEntity.ok(Map.of(
                "kind", "product-search", "query", request.query().trim(), "results", List.of())));
    }

    @PostMapping("/brew-plan")
    public ResponseEntity<?> brewPlan(@RequestBody BrewPlanRequest request,
                                      @RequestHeader(value = "x-agent-gateway-secret", required = false) String secret) {
        if (request.productId() == null || request.productId().isBlank() || request.productId().length() > 100
                || request.quantity() < 1 || request.quantity() > 10000) {
            return ResponseEntity.badRequest().build();
        }
        return authorized(request, secret, () -> ResponseEntity.ok(Map.of(
                "kind", "brew-plan", "productId", request.productId(),
                "quantity", request.quantity(), "planVersion", "local-1")));
    }

    @PostMapping("/provenance")
    public ResponseEntity<?> provenance(@RequestBody ProvenanceRequest request,
                                        @RequestHeader(value = "x-agent-gateway-secret", required = false) String secret) {
        if (request.productId() == null || request.productId().isBlank() || request.productId().length() > 100) {
            return ResponseEntity.badRequest().build();
        }
        return authorized(request, secret, () -> ResponseEntity.ok(Map.of(
                "kind", "provenance-report", "productId", request.productId(),
                "evidence", List.of(), "evidenceStatus", "not-seeded")));
    }

    @PostMapping("/wholesale-quote")
    public ResponseEntity<?> wholesaleQuote(@RequestBody WholesaleQuoteRequest request,
                                            @RequestHeader(value = "x-agent-gateway-secret", required = false) String secret) {
        if (request.productId() == null || request.productId().isBlank() || request.productId().length() > 100
                || request.quantity() < 1 || request.quantity() > 10000) {
            return ResponseEntity.badRequest().build();
        }
        return authorized(request, secret, () -> ResponseEntity.ok(Map.of(
                "kind", "wholesale-quote", "productId", request.productId(),
                "quantity", request.quantity(), "currency", "USDC",
                "totalMinor", (long) request.quantity() * 200_000L,
                "quoteVersion", "local-1", "expiresInSeconds", 900)));
    }

    private ResponseEntity<?> authorized(AgentRequest request, String secret, Supplier<ResponseEntity<?>> response) {
        if (serviceSecret.isBlank() || !fixedTimeEquals(serviceSecret, secret == null ? "" : secret)) {
            return ResponseEntity.status(401).build();
        }
        String problem = validate(request);
        if (problem != null) {
            return ResponseEntity.badRequest().body(Map.of("error", problem));
        }
        ResponseEntity<?> result = response.get();
        return ResponseEntity.status(result.getStatusCode())
                .header("x-correlation-id", request.correlationId())
                .body(result.getBody());
    }

    private static String validate(AgentRequest request) {
        if (request == null || request.correlationId() == null || request.correlationId().isBlank()
                || request.correlationId().length() > 96) {
            return "correlation id required";
        }
        if (request.requestHash() != null && !request.requestHash().matches("[0-9a-fA-F]{64}")) {
            return "request hash must be sha256 hex";
        }
        return null;
    }

    static boolean fixedTimeEquals(String expected, String supplied) {
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = supplied.getBytes(StandardCharsets.UTF_8);
        return a.length == b.length && MessageDigest.isEqual(a, b);
    }

    public interface AgentRequest {
        String correlationId();

        String requestHash();
    }

    public record SearchProductsRequest(String query, String correlationId, String requestHash) implements AgentRequest {
    }

    public record BrewPlanRequest(String productId, int quantity, String correlationId, String requestHash) implements AgentRequest {
    }

    public record ProvenanceRequest(String productId, String correlationId, String requestHash) implements AgentRequest {
    }

    public record WholesaleQuoteRequest(String productId, int quantity, String correlationId, String requestHash) implements AgentRequest {
    }
}

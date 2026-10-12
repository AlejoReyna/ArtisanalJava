package dev.alexisreyna.artisanalbrew.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.application.repository.WalletStatusEventRepository;
import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Port of {@code WalletStatusController} ({@code Web/Controllers}). */
@RestController
@RequestMapping("/api/wallet-status")
public class WalletStatusController {
    private static final Logger log = LoggerFactory.getLogger(WalletStatusController.class);

    private final WalletStatusEventRepository repository;
    private final SqsMessagePublisher publisher;
    private final ObjectMapper mapper;

    public WalletStatusController(WalletStatusEventRepository repository, SqsMessagePublisher publisher,
                                  ObjectMapper mapper) {
        this.repository = repository;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody WalletStatusRequest request) {
        String walletAddress = WalletAddressRules.tryNormalizeWallet(request.walletAddress()).orElse(null);
        if (walletAddress == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid walletAddress is required."));
        }
        if (request.status() == null || request.status().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "status is required."));
        }
        WalletStatusEvent event = new WalletStatusEvent();
        event.setWalletAddress(walletAddress);
        event.setStatus(request.status().trim());
        event.setEventType(request.eventType() == null || request.eventType().isBlank() ? null : request.eventType().trim());
        try {
            event.setPayloadJson(request.payload() == null ? null : mapper.writeValueAsString(request.payload()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "payload must be valid JSON."));
        }
        event.setCreatedAt(Instant.now());
        repository.add(event);

        String awsMessageId = null;
        try {
            awsMessageId = publisher.publish(Map.of(
                            "walletAddress", event.getWalletAddress(),
                            "status", event.getStatus(),
                            "createdAt", event.getCreatedAt().toString()))
                    .orElse(null);
            if (awsMessageId != null && !awsMessageId.isBlank()) {
                repository.markPublishedToAws(event.getId(), awsMessageId, Instant.now());
            }
        } catch (Exception e) {
            log.error("Wallet status event was stored but could not be published. Id={}", event.getId(), e);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", event.getId());
        body.put("walletAddress", event.getWalletAddress());
        body.put("status", event.getStatus());
        body.put("eventType", event.getEventType());
        body.put("createdAt", event.getCreatedAt().toString());
        body.put("publishedToAws", awsMessageId != null && !awsMessageId.isBlank());
        body.put("awsMessageId", awsMessageId);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{walletAddress}")
    public ResponseEntity<?> latest(@PathVariable String walletAddress) {
        String normalized = WalletAddressRules.tryNormalizeWallet(walletAddress).orElse(null);
        if (normalized == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid walletAddress is required."));
        }
        return repository.getLatestForWallet(normalized)
                .map(event -> ResponseEntity.ok((Object) describe(event)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, Object> describe(WalletStatusEvent event) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", event.getId());
        body.put("walletAddress", event.getWalletAddress());
        body.put("status", event.getStatus());
        body.put("eventType", event.getEventType());
        body.put("payload", parse(event.getPayloadJson()));
        body.put("createdAt", event.getCreatedAt().toString());
        body.put("publishedToAwsAtUtc", event.getPublishedToAwsAtUtc() == null ? null : event.getPublishedToAwsAtUtc().toString());
        body.put("awsMessageId", event.getAwsMessageId());
        return body;
    }

    private JsonNode parse(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return mapper.readTree(payloadJson);
        } catch (Exception e) {
            return null;
        }
    }

    public record WalletStatusRequest(String walletAddress, String status, String eventType, JsonNode payload) {
    }
}

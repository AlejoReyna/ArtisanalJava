package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/WalletStatusEvent.cs}. */
public class WalletStatusEvent {
    private UUID id = UUID.randomUUID();
    private String walletAddress = "";
    private String status = "";
    private String eventType;
    private String payloadJson;
    private Instant createdAt = Instant.now();
    private Instant publishedToAwsAtUtc;
    private String awsMessageId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String v) { this.walletAddress = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getEventType() { return eventType; }
    public void setEventType(String v) { this.eventType = v; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String v) { this.payloadJson = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getPublishedToAwsAtUtc() { return publishedToAwsAtUtc; }
    public void setPublishedToAwsAtUtc(Instant v) { this.publishedToAwsAtUtc = v; }
    public String getAwsMessageId() { return awsMessageId; }
    public void setAwsMessageId(String v) { this.awsMessageId = v; }
}

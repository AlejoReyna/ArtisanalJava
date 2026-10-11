package dev.alexisreyna.artisanalbrew.domain.entities;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/Receipt.cs}. */
public class Receipt {
    private UUID id = UUID.randomUUID();
    private UUID orderId;
    private String fileUrl = "";
    private Instant createdAt = Instant.now();
    private Instant emailSentAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getEmailSentAt() { return emailSentAt; }
    public void setEmailSentAt(Instant emailSentAt) { this.emailSentAt = emailSentAt; }
}

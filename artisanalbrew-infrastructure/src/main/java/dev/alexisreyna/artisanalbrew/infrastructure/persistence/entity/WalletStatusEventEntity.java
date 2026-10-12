package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code wallet_status_events} (the one snake_case table). */
@Entity
@Table(name = "\"wallet_status_events\"")
public class WalletStatusEventEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"WalletAddress\"")
    public String walletAddress;

    @Column(name = "\"Status\"")
    public String status;

    @Column(name = "\"EventType\"")
    public String eventType;

    @Column(name = "\"PayloadJson\"")
    public String payloadJson;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"PublishedToAwsAtUtc\"")
    public Instant publishedToAwsAtUtc;

    @Column(name = "\"AwsMessageId\"")
    public String awsMessageId;
}

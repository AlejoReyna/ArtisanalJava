package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.WalletStatusEventRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletStatusEvent;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.WalletStatusEventEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.WalletStatusEventJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA adapter for {@link WalletStatusEventRepository}. */
@Repository
@Transactional
public class JpaWalletStatusEventRepositoryAdapter implements WalletStatusEventRepository {
    private final WalletStatusEventJpaRepository jpa;

    public JpaWalletStatusEventRepositoryAdapter(WalletStatusEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void add(WalletStatusEvent event) {
        jpa.save(toEntity(event));
    }

    @Override
    public void markPublishedToAws(UUID id, String awsMessageId, Instant publishedAtUtc) {
        WalletStatusEventEntity e = jpa.findById(id).orElseThrow(
                () -> new IllegalStateException("Wallet status event not found: " + id));
        e.publishedToAwsAtUtc = publishedAtUtc;
        e.awsMessageId = awsMessageId;
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WalletStatusEvent> getLatestForWallet(String walletAddress) {
        return jpa.findFirstByWalletAddressOrderByCreatedAtDesc(walletAddress)
                .map(JpaWalletStatusEventRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletStatusEvent> getRecent(int count) {
        return jpa.findTop25ByOrderByCreatedAtDesc().stream().limit(count)
                .map(JpaWalletStatusEventRepositoryAdapter::toDomain).toList();
    }

    static WalletStatusEventEntity toEntity(WalletStatusEvent event) {
        WalletStatusEventEntity e = new WalletStatusEventEntity();
        e.id = event.getId();
        e.walletAddress = event.getWalletAddress();
        e.status = event.getStatus();
        e.eventType = event.getEventType();
        e.payloadJson = event.getPayloadJson();
        e.createdAt = event.getCreatedAt();
        e.publishedToAwsAtUtc = event.getPublishedToAwsAtUtc();
        e.awsMessageId = event.getAwsMessageId();
        return e;
    }

    static WalletStatusEvent toDomain(WalletStatusEventEntity e) {
        WalletStatusEvent event = new WalletStatusEvent();
        event.setId(e.id);
        event.setWalletAddress(e.walletAddress);
        event.setStatus(e.status);
        event.setEventType(e.eventType);
        event.setPayloadJson(e.payloadJson);
        event.setCreatedAt(e.createdAt);
        event.setPublishedToAwsAtUtc(e.publishedToAwsAtUtc);
        event.setAwsMessageId(e.awsMessageId);
        return event;
    }
}

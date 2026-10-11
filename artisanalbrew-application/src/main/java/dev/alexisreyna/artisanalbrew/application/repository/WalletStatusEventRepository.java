package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.WalletStatusEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port of {@code Repositories/IWalletStatusEventRepository.cs}. */
public interface WalletStatusEventRepository {
    void add(WalletStatusEvent event);

    void markPublishedToAws(UUID id, String awsMessageId, Instant publishedAtUtc);

    Optional<WalletStatusEvent> getLatestForWallet(String walletAddress);

    List<WalletStatusEvent> getRecent(int count);
}

package dev.alexisreyna.artisanalbrew.application.repository;

import java.util.UUID;

/** Port of {@code ApplicationUserProfileSnapshot} in {@code Repositories/IUserProfileRepository.cs}. */
public record ApplicationUserProfileSnapshot(
        String applicationUserId,
        UUID userProfileId,
        String email,
        String userName,
        String walletAddress,
        Integer walletChainId) {
}

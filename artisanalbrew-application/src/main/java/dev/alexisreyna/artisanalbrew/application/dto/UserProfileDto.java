package dev.alexisreyna.artisanalbrew.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code DTOs/UserProfileDto.cs}. */
public record UserProfileDto(
        UUID userProfileId,
        String displayName,
        String email,
        String walletAddress,
        Integer walletChainId,
        Instant createdAt,
        String role,
        RobotAvatarDto avatar) {
}

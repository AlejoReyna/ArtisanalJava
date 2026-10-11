package dev.alexisreyna.artisanalbrew.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Port of {@code DTOs/ProfileDashboardDto.cs}. */
public record ProfileDashboardDto(
        UUID userProfileId,
        String displayName,
        String email,
        String walletAddress,
        Integer walletChainId,
        Instant createdAt,
        String role,
        int totalOrders,
        RobotAvatarDto avatar) {
}

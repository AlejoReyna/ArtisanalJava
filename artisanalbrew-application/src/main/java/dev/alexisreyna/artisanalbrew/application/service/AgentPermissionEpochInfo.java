package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.domain.entities.AgentPermissionEpochStatus;

import java.time.Instant;
import java.util.List;

/** Port of {@code AgentPermissionEpochInfo} in {@code Services/ISmartAccountService.cs}. */
public record AgentPermissionEpochInfo(
        String chainKey,
        String delegatorAddress,
        String ownerAddress,
        String agentAddress,
        String epoch,
        Instant validAfterUtc,
        Instant validBeforeUtc,
        AgentPermissionEpochStatus status,
        Instant installedAtUtc,
        String installedTxHash,
        Instant revokedAtUtc,
        String revokedTxHash,
        List<AgentPermissionGrantInfo> grants) {
}

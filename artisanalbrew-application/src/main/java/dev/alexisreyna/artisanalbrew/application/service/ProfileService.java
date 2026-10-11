package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.ProfileDashboardDto;
import dev.alexisreyna.artisanalbrew.application.dto.RobotAvatarDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UserProfileDto;

import java.util.UUID;

/** Port of {@code Services/IProfileService.cs}. */
public interface ProfileService {
    UUID ensureProfileLinked(String applicationUserId);

    ProfileDashboardDto getDashboard(UUID userProfileId);

    /**
     * Read-only by design: never creates a profile as a side effect of rendering.
     * Unlinked accounts render a wallet-seeded robot.
     */
    RobotAvatarDto getAvatarForApplicationUser(String applicationUserId);

    UserProfileDto updateDisplayName(UUID userProfileId, UpdateUserProfileRequest request);

    UserProfileDto updateAvatar(UUID userProfileId, UpdateAvatarRequest request);

    /** Clears the saved look so the profile tracks its wallet seed again. */
    UserProfileDto resetAvatar(UUID userProfileId);

    void deleteAccount(UUID userProfileId);
}

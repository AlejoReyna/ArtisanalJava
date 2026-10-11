package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.ProfileDashboardDto;
import dev.alexisreyna.artisanalbrew.application.dto.RobotAvatarDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UserProfileDto;
import dev.alexisreyna.artisanalbrew.application.repository.ApplicationUserProfileSnapshot;
import dev.alexisreyna.artisanalbrew.application.repository.UserProfileRepository;
import dev.alexisreyna.artisanalbrew.application.validation.AvatarValidators;
import dev.alexisreyna.artisanalbrew.application.validation.ProfileValidators;
import dev.alexisreyna.artisanalbrew.domain.avatars.RobotAvatar;
import dev.alexisreyna.artisanalbrew.domain.entities.Cart;
import dev.alexisreyna.artisanalbrew.domain.entities.UserProfile;
import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Port of {@code ProfileService} in {@code Services/ProfileService.cs}.
 */
public final class DefaultProfileService implements ProfileService {
    private final UserProfileRepository profiles;

    public DefaultProfileService(UserProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Override
    public UUID ensureProfileLinked(String applicationUserId) {
        if (applicationUserId == null || applicationUserId.isBlank()) {
            throw new IllegalArgumentException("Application user id is required.");
        }
        ApplicationUserProfileSnapshot snapshot = profiles.getApplicationUserProfile(applicationUserId)
                .orElseThrow(() -> new IllegalStateException("The authenticated user could not be found."));
        if (snapshot.userProfileId() != null) {
            return snapshot.userProfileId();
        }
        String walletAddress = snapshot.walletAddress() != null ? snapshot.walletAddress()
                : snapshot.userName() != null ? snapshot.userName() : applicationUserId;
        UserProfile profile = new UserProfile();
        profile.setEmail(createSyntheticEmail(walletAddress));
        profile.setDisplayName(createDefaultDisplayName(walletAddress));
        profile.setRole(UserRole.CUSTOMER);
        profile.setCreatedAt(Instant.now());
        Cart cart = new Cart();
        cart.setUserProfileId(profile.getId());
        cart.setCreatedAt(Instant.now());
        return profiles.createProfileAndCartForApplicationUser(applicationUserId, profile, cart);
    }

    @Override
    public ProfileDashboardDto getDashboard(UUID userProfileId) {
        UserProfile profile = profiles.getById(userProfileId)
                .orElseThrow(() -> new IllegalStateException("The requested profile could not be found."));
        ApplicationUserProfileSnapshot snapshot =
                profiles.getApplicationUserProfileByProfileId(userProfileId).orElse(null);
        int totalOrders = profiles.countOrders(userProfileId);
        String wallet = snapshot == null ? null : snapshot.walletAddress();
        return new ProfileDashboardDto(
                profile.getId(), profile.getDisplayName(), profile.getEmail(), wallet,
                snapshot == null ? null : snapshot.walletChainId(),
                profile.getCreatedAt(), profile.getRole().name(), totalOrders,
                RobotAvatarDto.resolve(profile.getAvatar(), wallet));
    }

    @Override
    public RobotAvatarDto getAvatarForApplicationUser(String applicationUserId) {
        if (applicationUserId == null || applicationUserId.isBlank()) {
            return RobotAvatarDto.resolve(null, null);
        }
        ApplicationUserProfileSnapshot snapshot =
                profiles.getApplicationUserProfile(applicationUserId).orElse(null);
        if (snapshot == null) {
            return RobotAvatarDto.resolve(null, null);
        }
        // Unlinked accounts render from the wallet seed — no row is created to answer this.
        UserProfile profile = snapshot.userProfileId() == null
                ? null
                : profiles.getById(snapshot.userProfileId()).orElse(null);
        return RobotAvatarDto.resolve(profile == null ? null : profile.getAvatar(), snapshot.walletAddress());
    }

    @Override
    public UserProfileDto updateDisplayName(UUID userProfileId, UpdateUserProfileRequest request) {
        UpdateUserProfileRequest normalized =
                new UpdateUserProfileRequest(request.displayName() == null ? null : request.displayName().trim());
        ProfileValidators.validateUpdateDisplayName(normalized);
        UserProfile profile = profiles.getById(userProfileId)
                .orElseThrow(() -> new IllegalStateException("The requested profile could not be found."));
        profile.setDisplayName(normalized.displayName());
        profiles.update(profile);
        return describe(profile);
    }

    @Override
    public UserProfileDto updateAvatar(UUID userProfileId, UpdateAvatarRequest request) {
        AvatarValidators.validateUpdate(request);
        UserProfile profile = profiles.getById(userProfileId)
                .orElseThrow(() -> new IllegalStateException("The requested profile could not be found."));
        // Stored exactly as chosen; reads normalize, writes do not.
        profile.setAvatar(request.toRobotAvatar());
        profiles.update(profile);
        return describe(profile);
    }

    @Override
    public UserProfileDto resetAvatar(UUID userProfileId) {
        UserProfile profile = profiles.getById(userProfileId)
                .orElseThrow(() -> new IllegalStateException("The requested profile could not be found."));
        // Null, not the seed's current values: back to *tracking* the seed.
        profile.setAvatar((RobotAvatar) null);
        profiles.update(profile);
        return describe(profile);
    }

    @Override
    public void deleteAccount(UUID userProfileId) {
        profiles.deleteProfileCascade(userProfileId);
    }

    private UserProfileDto describe(UserProfile profile) {
        ApplicationUserProfileSnapshot snapshot =
                profiles.getApplicationUserProfileByProfileId(profile.getId()).orElse(null);
        String wallet = snapshot == null ? null : snapshot.walletAddress();
        return new UserProfileDto(
                profile.getId(), profile.getDisplayName(), profile.getEmail(), wallet,
                snapshot == null ? null : snapshot.walletChainId(),
                profile.getCreatedAt(), profile.getRole().name(),
                RobotAvatarDto.resolve(profile.getAvatar(), wallet));
    }

    private static String createSyntheticEmail(String walletAddress) {
        return walletAddress.trim().toLowerCase(java.util.Locale.ROOT) + "@wallet.thiscafeteria.local";
    }

    private static String createDefaultDisplayName(String walletAddress) {
        String trimmed = walletAddress.trim();
        return trimmed.length() >= 12
                ? trimmed.substring(0, 6) + "..." + trimmed.substring(trimmed.length() - 4)
                : trimmed;
    }
}

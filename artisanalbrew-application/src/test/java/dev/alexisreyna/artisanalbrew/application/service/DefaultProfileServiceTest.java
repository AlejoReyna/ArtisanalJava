package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.RobotAvatarDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;
import dev.alexisreyna.artisanalbrew.application.repository.ApplicationUserProfileSnapshot;
import dev.alexisreyna.artisanalbrew.application.repository.UserProfileRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Cart;
import dev.alexisreyna.artisanalbrew.domain.entities.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultProfileServiceTest {

    static final class FakeProfiles implements UserProfileRepository {
        final Map<String, ApplicationUserProfileSnapshot> snapshots = new HashMap<>();
        final Map<UUID, UserProfile> profiles = new HashMap<>();
        int orders;

        @Override
        public Optional<ApplicationUserProfileSnapshot> getApplicationUserProfile(String applicationUserId) {
            return Optional.ofNullable(snapshots.get(applicationUserId));
        }

        @Override
        public Optional<ApplicationUserProfileSnapshot> getApplicationUserProfileByProfileId(UUID userProfileId) {
            return snapshots.values().stream().filter(s -> userProfileId.equals(s.userProfileId())).findFirst();
        }

        @Override
        public Optional<UserProfile> getById(UUID id) {
            return Optional.ofNullable(profiles.get(id));
        }

        @Override
        public UUID createProfileAndCartForApplicationUser(String applicationUserId, UserProfile profile, Cart cart) {
            profiles.put(profile.getId(), profile);
            ApplicationUserProfileSnapshot old = snapshots.get(applicationUserId);
            snapshots.put(applicationUserId, new ApplicationUserProfileSnapshot(
                    applicationUserId, profile.getId(),
                    old == null ? null : old.email(), old == null ? null : old.userName(),
                    old == null ? null : old.walletAddress(), old == null ? null : old.walletChainId()));
            return profile.getId();
        }

        @Override
        public void update(UserProfile profile) {
            profiles.put(profile.getId(), profile);
        }

        @Override
        public int countOrders(UUID userProfileId) {
            return orders;
        }

        @Override
        public void deleteProfileCascade(UUID userProfileId) {
            profiles.remove(userProfileId);
        }
    }

    @Test
    void ensureLinkedReturnsExistingOrCreatesWithSyntheticIdentity() {
        FakeProfiles repos = new FakeProfiles();
        UUID existingId = UUID.randomUUID();
        repos.snapshots.put("user-1", new ApplicationUserProfileSnapshot("user-1", existingId,
                "a@b.c", "user-1", "0xabc", 11155111));
        DefaultProfileService service = new DefaultProfileService(repos);

        assertThat(service.ensureProfileLinked("user-1")).isEqualTo(existingId);
        assertThatThrownBy(() -> service.ensureProfileLinked(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.ensureProfileLinked("ghost"))
                .isInstanceOf(IllegalStateException.class);

        repos.snapshots.put("user-2", new ApplicationUserProfileSnapshot("user-2", null,
                null, null, "0xABCDEF1234567890", null));
        UUID created = service.ensureProfileLinked("user-2");
        UserProfile profile = repos.profiles.get(created);
        assertThat(profile.getEmail()).isEqualTo("0xabcdef1234567890@wallet.thiscafeteria.local");
        assertThat(profile.getDisplayName()).isEqualTo("0xABCD...7890");
    }

    @Test
    void avatarReadNeverCreatesAndTracksSeedAfterReset() {
        FakeProfiles repos = new FakeProfiles();
        DefaultProfileService service = new DefaultProfileService(repos);
        assertThat(repos.profiles).isEmpty();
        assertThat(service.getAvatarForApplicationUser("ghost"))
                .isEqualTo(RobotAvatarDto.resolve(null, null));

        String wallet = "0xabcdef1234567890abcdef1234567890abcdef12";
        repos.snapshots.put("user-1", new ApplicationUserProfileSnapshot("user-1", null, null, null, wallet, 11155111));
        // Still no profile row after a read.
        assertThat(service.getAvatarForApplicationUser("user-1"))
                .isEqualTo(RobotAvatarDto.resolve(null, wallet));
        assertThat(repos.profiles).isEmpty();

        UUID id = service.ensureProfileLinked("user-1");
        service.updateAvatar(id, new UpdateAvatarRequest("gold", "graphite", "apron", "shades", "crown", "mug"));
        assertThat(service.getAvatarForApplicationUser("user-1").chassis()).isEqualTo("graphite");
        service.resetAvatar(id);
        assertThat(service.getAvatarForApplicationUser("user-1"))
                .isEqualTo(RobotAvatarDto.resolve(null, wallet));
    }

    @Test
    void displayNameIsTrimmedBeforeValidation() {
        FakeProfiles repos = new FakeProfiles();
        UUID id = UUID.randomUUID();
        UserProfile profile = new UserProfile();
        profile.setId(id);
        profile.setDisplayName("Old");
        repos.profiles.put(id, profile);
        repos.snapshots.put("user-1", new ApplicationUserProfileSnapshot("user-1", id, "a@b.c", "u", null, null));
        DefaultProfileService service = new DefaultProfileService(repos);
        var dto = service.updateDisplayName(id, new UpdateUserProfileRequest("  Alex  "));
        assertThat(dto.displayName()).isEqualTo("Alex");
        assertThat(service.getDashboard(id).totalOrders()).isEqualTo(0);
    }
}

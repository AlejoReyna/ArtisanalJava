package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.Cart;
import dev.alexisreyna.artisanalbrew.domain.entities.UserProfile;

import java.util.Optional;
import java.util.UUID;

/** Port of {@code Repositories/IUserProfileRepository.cs}. */
public interface UserProfileRepository {
    Optional<ApplicationUserProfileSnapshot> getApplicationUserProfile(String applicationUserId);

    Optional<ApplicationUserProfileSnapshot> getApplicationUserProfileByProfileId(UUID userProfileId);

    Optional<UserProfile> getById(UUID id);

    UUID createProfileAndCartForApplicationUser(String applicationUserId, UserProfile profile, Cart cart);

    void update(UserProfile profile);

    int countOrders(UUID userProfileId);

    void deleteProfileCascade(UUID userProfileId);
}

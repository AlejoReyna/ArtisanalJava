package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.ApplicationUserProfileSnapshot;
import dev.alexisreyna.artisanalbrew.application.repository.UserProfileRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Cart;
import dev.alexisreyna.artisanalbrew.domain.entities.UserProfile;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert.AvatarJson;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CartEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserProfileEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CartItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CartJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponRedemptionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.TransparencyRecordJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for {@link UserProfileRepository}. The application-user side
 * reads the mapped {@code AspNetUsers} subset; full Identity arrives in Phase 4.
 */
@Repository
@Transactional
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {
    private final UserProfileJpaRepository profiles;
    private final ApplicationUserJpaRepository appUsers;
    private final CartJpaRepository carts;
    private final CartItemJpaRepository cartItems;
    private final OrderJpaRepository orders;
    private final OrderItemJpaRepository orderItems;
    private final TransparencyRecordJpaRepository transparency;
    private final CouponRedemptionJpaRepository redemptions;

    public JpaUserProfileRepositoryAdapter(UserProfileJpaRepository profiles, ApplicationUserJpaRepository appUsers,
                                           CartJpaRepository carts, CartItemJpaRepository cartItems,
                                           OrderJpaRepository orders, OrderItemJpaRepository orderItems,
                                           TransparencyRecordJpaRepository transparency,
                                           CouponRedemptionJpaRepository redemptions) {
        this.profiles = profiles;
        this.appUsers = appUsers;
        this.carts = carts;
        this.cartItems = cartItems;
        this.orders = orders;
        this.orderItems = orderItems;
        this.transparency = transparency;
        this.redemptions = redemptions;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationUserProfileSnapshot> getApplicationUserProfile(String applicationUserId) {
        return appUsers.findById(applicationUserId).map(JpaUserProfileRepositoryAdapter::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationUserProfileSnapshot> getApplicationUserProfileByProfileId(UUID userProfileId) {
        return appUsers.findByUserProfileId(userProfileId).map(JpaUserProfileRepositoryAdapter::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfile> getById(UUID id) {
        return profiles.findById(id).map(JpaUserProfileRepositoryAdapter::toDomain);
    }

    @Override
    public UUID createProfileAndCartForApplicationUser(String applicationUserId, UserProfile profile, Cart cart) {
        profiles.save(toEntity(profile));
        CartEntity cartEntity = new CartEntity();
        cartEntity.id = cart.getId();
        cartEntity.userProfileId = profile.getId();
        cartEntity.createdAt = cart.getCreatedAt();
        cartEntity.updatedAt = cart.getUpdatedAt();
        carts.save(cartEntity);
        ApplicationUserEntity appUser = appUsers.findById(applicationUserId).orElseThrow(
                () -> new IllegalStateException("The authenticated user could not be found."));
        appUser.userProfileId = profile.getId();
        appUsers.save(appUser);
        return profile.getId();
    }

    @Override
    public void update(UserProfile profile) {
        profiles.save(toEntity(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public int countOrders(UUID userProfileId) {
        return (int) orders.countByUserProfileId(userProfileId);
    }

    @Override
    public void deleteProfileCascade(UUID userProfileId) {
        // Orders first: Order -> UserProfile is RESTRICT, so explicit delete.
        // Cascades to items/transparency/redemptions below mirror the FK graph.
        List<UUID> orderIds = orders.findByUserProfileId(userProfileId).stream().map(o -> o.id).toList();
        if (!orderIds.isEmpty()) {
            redemptions.findByUserProfileId(userProfileId).forEach(r -> redemptions.deleteById(r.id));
            transparency.deleteByOrderIdIn(orderIds);
            orderItems.deleteByOrderIdIn(orderIds);
            orders.deleteByUserProfileId(userProfileId);
        }
        carts.findByUserProfileId(userProfileId).ifPresent(cart -> {
            cartItems.deleteByCartId(cart.id);
            carts.deleteById(cart.id);
        });
        appUsers.findAllByUserProfileId(userProfileId).forEach(appUser -> {
            appUser.userProfileId = null;
            appUsers.save(appUser);
        });
        profiles.deleteById(userProfileId);
    }

    static ApplicationUserProfileSnapshot toSnapshot(ApplicationUserEntity e) {
        return new ApplicationUserProfileSnapshot(e.id, e.userProfileId, e.email, e.userName,
                e.walletAddress, e.walletChainId);
    }

    static UserProfileEntity toEntity(UserProfile profile) {
        UserProfileEntity e = new UserProfileEntity();
        e.id = profile.getId();
        e.email = profile.getEmail();
        e.displayName = profile.getDisplayName();
        e.role = profile.getRole();
        e.createdAt = profile.getCreatedAt();
        e.avatarJson = AvatarJson.toJson(profile.getAvatar());
        return e;
    }

    static UserProfile toDomain(UserProfileEntity e) {
        UserProfile profile = new UserProfile();
        profile.setId(e.id);
        profile.setEmail(e.email);
        profile.setDisplayName(e.displayName);
        profile.setRole(e.role);
        profile.setCreatedAt(e.createdAt);
        profile.setAvatar(AvatarJson.fromJson(e.avatarJson));
        return profile;
    }
}

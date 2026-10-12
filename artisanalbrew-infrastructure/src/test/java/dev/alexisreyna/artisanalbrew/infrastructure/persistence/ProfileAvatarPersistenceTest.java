package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.application.dto.RobotAvatarDto;
import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.application.service.DefaultProfileService;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaUserProfileRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CartItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CartJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.CouponRedemptionJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderItemJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.OrderJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.TransparencyRecordJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Profile + avatar JSON round-trip through the database, driven by the real
 * {@code DefaultProfileService} over the JPA adapter.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class ProfileAvatarPersistenceTest {

    @Autowired
    private UserProfileJpaRepository profiles;
    @Autowired
    private ApplicationUserJpaRepository appUsers;
    @Autowired
    private CartJpaRepository carts;
    @Autowired
    private CartItemJpaRepository cartItems;
    @Autowired
    private OrderJpaRepository orders;
    @Autowired
    private OrderItemJpaRepository orderItems;
    @Autowired
    private TransparencyRecordJpaRepository transparency;
    @Autowired
    private CouponRedemptionJpaRepository redemptions;

    private DefaultProfileService service() {
        return new DefaultProfileService(new JpaUserProfileRepositoryAdapter(profiles, appUsers, carts,
                cartItems, orders, orderItems, transparency, redemptions));
    }

    @Test
    void avatarSurvivesDatabaseRoundTrip() {
        ApplicationUserEntity appUser = new ApplicationUserEntity();
        appUser.id = "auth-user-1";
        appUser.userName = "0xABCDEF1234567890";
        appUser.walletAddress = "0xABCDEF1234567890";
        appUser.walletChainId = 11155111;
        appUsers.save(appUser);

        UUID profileId = service().ensureProfileLinked("auth-user-1");
        assertThat(carts.findByUserProfileId(profileId)).isPresent();

        service().updateAvatar(profileId,
                new UpdateAvatarRequest("gold", "graphite", "apron", "shades", "crown", "mug"));
        RobotAvatarDto read = service().getAvatarForApplicationUser("auth-user-1");
        assertThat(read.chassis()).isEqualTo("graphite");
        assertThat(read.hold()).isEqualTo("mug");

        // Raw JSON keeps the .NET PascalCase shape.
        String raw = profiles.findById(profileId).orElseThrow().avatarJson;
        assertThat(raw).contains("\"Chassis\":\"graphite\"");

        service().resetAvatar(profileId);
        assertThat(service().getAvatarForApplicationUser("auth-user-1"))
                .isEqualTo(RobotAvatarDto.resolve(null, "0xABCDEF1234567890"));
    }
}

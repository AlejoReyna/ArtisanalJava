package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.service.IdentityAccountService;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.TestApplication;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaWalletIdentityRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter.JpaWalletIdentityRepositoryAdapter;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.WalletIdentityJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Account lifecycle over a migrated database: admin seed, password login
 * with lockout, wallet link/create, cross-account link refusal, delete.
 */
@SpringBootTest(classes = TestApplication.class)
@Transactional
class IdentityAccountServiceTest {

    @Autowired
    private ApplicationUserJpaRepository users;
    @Autowired
    private RoleJpaRepository roles;
    @Autowired
    private UserRoleJpaRepository userRoles;
    @Autowired
    private UserProfileJpaRepository profiles;
    @Autowired
    private WalletIdentityJpaRepository identities;
    @Autowired
    private JpaWalletIdentityRepositoryAdapter walletIdentities;

    private IdentityAccountService service() {
        return new JpaIdentityAccountService(users, userRoles, roles,
                new JpaWalletIdentityRepositoryAdapter(identities), new AspNetIdentityPasswordEncoder());
    }

    private void seedAdmin() {
        new JpaAdminIdentitySeeder(users, roles, userRoles, profiles, new AspNetIdentityPasswordEncoder())
                .seed("admin@example.com", "Admin1234");
    }

    @Test
    void adminSeedIsIdempotentAndGrantsRole() {
        var seeder = new JpaAdminIdentitySeeder(users, roles, userRoles, profiles, new AspNetIdentityPasswordEncoder());
        seeder.seed("admin@example.com", "Admin1234");
        seeder.seed("admin@example.com", "Admin1234");
        var admin = users.findByNormalizedEmail("ADMIN@EXAMPLE.COM").orElseThrow();
        assertThat(admin.emailConfirmed).isTrue();
        assertThat(admin.userProfileId).isNotNull();
        assertThat(roles.findByNormalizedName("ADMIN")).isPresent();
        assertThat(roles.findByNormalizedName("CUSTOMER")).isPresent();
        var adminRole = roles.findByNormalizedName("ADMIN").orElseThrow();
        assertThat(userRoles.existsByUserIdAndRoleId(admin.id, adminRole.id)).isTrue();
        assertThat(profiles.findById(admin.userProfileId).orElseThrow().role.name()).isEqualTo("ADMIN");
    }

    @Test
    void passwordLoginEnforcesLockout() {
        seedAdmin();
        var service = service();
        assertThat(service.passwordSignIn("admin@example.com", "Admin1234", false)).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        service.signOut();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        for (int i = 0; i < 5; i++) {
            assertThat(service.passwordSignIn("admin@example.com", "wrong", false)).isFalse();
        }
        // Locked out: even the right password fails now.
        assertThat(service.passwordSignIn("admin@example.com", "Admin1234", false)).isFalse();
        assertThat(service.passwordSignIn("nobody@example.com", "x", false)).isFalse();
    }

    @Test
    void walletLinkCreatesAndRefusesCrossAccountTakeover() {
        var service = service();
        var first = service.findOrCreateAndLinkWallet("0xABCDEF1234567890", 11155111, "metamask", "Evm");
        assertThat(first.succeeded()).isTrue();
        var same = service.findOrCreateAndLinkWallet("0xabcdef1234567890", 11155111, "metamask", "Evm");
        assertThat(same.succeeded()).isTrue();
        assertThat(same.account().id()).isEqualTo(first.account().id());

        // A second account linking the same wallet must fail without stealing it.
        var second = service.findOrCreateAndLinkWallet("0xSOLANAADDRESS1111111111111111111111", null, null, "Solana");
        assertThat(second.succeeded()).isTrue();
        service.signOut();
        assertThat(service.findById(first.account().id())).isPresent();
        assertThat(service.delete(first.account().id()).succeeded()).isTrue();
        assertThat(service.findById(first.account().id())).isEmpty();
        assertThat(service.delete(first.account().id()).succeeded()).isFalse();
    }

    @Test
    void walletIdentityRefusesTakeover() {
        var service = service();
        String walletA = "0x" + "aa".repeat(20);
        String walletB = "0x" + "bb".repeat(20);
        var a = service.findOrCreateAndLinkWallet(walletA, 11155111, "metamask", "Evm");
        var b = service.findOrCreateAndLinkWallet(walletB, 11155111, "metamask", "Evm");
        assertThat(a.succeeded()).isTrue();
        assertThat(b.succeeded()).isTrue();
        // B re-linking A's wallet must fail without stealing it.
        assertThat(walletIdentities.upsert(b.account().id(), "Evm", walletA.toLowerCase(), walletA, "metamask"))
                .isFalse();
        assertThat(walletIdentities.upsert(a.account().id(), "Evm", walletA.toLowerCase(), walletA, "metamask"))
                .isTrue();
        assertThat(walletIdentities.find("Evm", walletA.toLowerCase()).orElseThrow().getUserId())
                .isEqualTo(a.account().id());
    }
}

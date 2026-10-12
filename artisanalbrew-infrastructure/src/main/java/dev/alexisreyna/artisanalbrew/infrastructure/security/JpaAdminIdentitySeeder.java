package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.ApplicationUserEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.RoleEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserProfileEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.UserRoleEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Port of {@code AdminIdentitySeeder} ({@code Infrastructure/Identity}).
 * Idempotent: ensures {@code Admin}/{@code Customer} roles, the admin account
 * with a linked admin profile, and the Admin role grant. No-op without both
 * {@code authentication.admin-email} and {@code authentication.admin-password}.
 *
 * <p>Deliberately non-final: {@code @Transactional} needs a proxy subclass,
 * unlike .NET {@code sealed} which costs nothing at runtime.</p>
 */
public class JpaAdminIdentitySeeder {
    private final ApplicationUserJpaRepository users;
    private final RoleJpaRepository roles;
    private final UserRoleJpaRepository userRoles;
    private final UserProfileJpaRepository profiles;
    private final PasswordEncoder passwords;

    public JpaAdminIdentitySeeder(ApplicationUserJpaRepository users, RoleJpaRepository roles,
                                  UserRoleJpaRepository userRoles, UserProfileJpaRepository profiles,
                                  PasswordEncoder passwords) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.profiles = profiles;
        this.passwords = passwords;
    }

    @Transactional
    public void seed(String adminEmail, String adminPassword) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        if (adminPassword.length() < 8) {
            throw new IllegalArgumentException("The admin password must be at least 8 characters.");
        }
        RoleEntity adminRole = ensureRole("Admin");
        ensureRole("Customer");

        ApplicationUserEntity admin = users.findByNormalizedEmail(adminEmail.toUpperCase(Locale.ROOT)).orElse(null);
        if (admin == null) {
            UserProfileEntity profile = new UserProfileEntity();
            profile.id = UUID.randomUUID();
            profile.email = adminEmail;
            profile.displayName = "This Cafeteria Admin";
            profile.role = UserRole.ADMIN;
            profile.createdAt = Instant.now();
            profiles.save(profile);

            admin = new ApplicationUserEntity();
            admin.id = UUID.randomUUID().toString();
            admin.userName = adminEmail;
            admin.normalizedUserName = adminEmail.toUpperCase(Locale.ROOT);
            admin.email = adminEmail;
            admin.normalizedEmail = adminEmail.toUpperCase(Locale.ROOT);
            admin.emailConfirmed = true;
            admin.securityStamp = UUID.randomUUID().toString();
            admin.passwordHash = passwords.encode(adminPassword);
            admin.userProfileId = profile.id;
            users.save(admin);
        }
        if (!userRoles.existsByUserIdAndRoleId(admin.id, adminRole.id)) {
            UserRoleEntity link = new UserRoleEntity();
            link.userId = admin.id;
            link.roleId = adminRole.id;
            userRoles.save(link);
        }
    }

    private RoleEntity ensureRole(String name) {
        String normalized = name.toUpperCase(Locale.ROOT);
        return roles.findByNormalizedName(normalized).orElseGet(() -> {
            RoleEntity role = new RoleEntity();
            role.id = UUID.randomUUID().toString();
            role.name = name;
            role.normalizedName = normalized;
            role.concurrencyStamp = UUID.randomUUID().toString();
            return roles.save(role);
        });
    }
}

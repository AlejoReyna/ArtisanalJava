package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.repository.WalletAuthChallengeRepository;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallengeService;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Web security composition (mirrors {@code Web/Program.cs} auth wiring).
 * Rule-for-rule mapping of the .NET attributes:
 * <ul>
 *   <li>{@code [Authorize]} on profile/orders-me/rewards-claim/wallet-status-POST
 *       → {@code authenticated()}.</li>
 *   <li>{@code RequireAdmin} policy (product writes, order delete, admin pages)
 *       → {@code hasRole("ADMIN")}.</li>
 *   <li>{@code XSRF-TOKEN} readable cookie + {@code X-CSRF-TOKEN} header →
 *       {@link CookieCsrfTokenRepository} with matching names.</li>
 *   <li>{@code /admin/login-action} form + 302s → form login handlers below.</li>
 * </ul>
 * Wallet endpoints and the 8-hour sliding cookie land with the Phase 5
 * controllers; the HTTP rules themselves live in {@link WebSecurityRules} so
 * web slice tests can enforce them without the data layer.
 */
@Configuration
@Import(WebSecurityRules.class)
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new AspNetIdentityPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(ApplicationUserJpaRepository users,
                                                 UserRoleJpaRepository userRoles,
                                                 RoleJpaRepository roles) {
        return new ApplicationUserDetailsService(users, userRoles, roles);
    }

    @Bean
    public JpaAdminIdentitySeeder adminIdentitySeeder(ApplicationUserJpaRepository users,
                                                      RoleJpaRepository roles,
                                                      UserRoleJpaRepository userRoles,
                                                      UserProfileJpaRepository profiles,
                                                      PasswordEncoder passwords) {
        return new JpaAdminIdentitySeeder(users, roles, userRoles, profiles, passwords);
    }

    @Bean
    public DefaultEvmWalletChallengeService evmWalletChallengeService() {
        return new DefaultEvmWalletChallengeService();
    }

    @Bean
    public SolanaWalletChallengeService solanaWalletChallengeService(
            WalletAuthChallengeRepository challenges) {
        return new DefaultSolanaWalletChallengeService(challenges);
    }

    @Bean
    public String adminSeedMarker(@Value("${authentication.admin-email:}") String email,
                                  @Value("${authentication.admin-password:}") String password,
                                  JpaAdminIdentitySeeder seeder) {
        seeder.seed(email, password);
        return "admin-seed-done";
    }
}

package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ApplicationUserJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.RoleJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserProfileJpaRepository;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.UserRoleJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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
 * controllers; rate limiting follows with the web module.
 */
@Configuration
@EnableWebSecurity
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokens = new CookieCsrfTokenRepository();
        csrfTokens.setCookieName("XSRF-TOKEN");
        csrfTokens.setHeaderName("X-CSRF-TOKEN");

        SimpleUrlAuthenticationSuccessHandler success = new SimpleUrlAuthenticationSuccessHandler("/admin");
        SimpleUrlAuthenticationFailureHandler failure =
                new SimpleUrlAuthenticationFailureHandler("/admin/login?error=1");

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokens)
                        .ignoringRequestMatchers("/admin/logout"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/products/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/me").authenticated()
                        .requestMatchers("/api/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/rewards/api/claim").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/wallet-status").authenticated()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginProcessingUrl("/admin/login-action")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(success)
                        .failureHandler(failure)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }

    @Bean
    public String adminSeedMarker(@Value("${authentication.admin-email:}") String email,
                                  @Value("${authentication.admin-password:}") String password,
                                  JpaAdminIdentitySeeder seeder) {
        seeder.seed(email, password);
        return "admin-seed-done";
    }
}

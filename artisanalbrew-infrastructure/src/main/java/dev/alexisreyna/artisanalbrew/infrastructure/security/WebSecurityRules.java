package dev.alexisreyna.artisanalbrew.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * The HTTP security rules on their own (no repository dependencies), so web
 * slice tests can enforce the real chain without the whole data layer.
 * Rule-for-rule mapping of the .NET attributes — see
 * {@link SecurityConfiguration} for the rationale.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityRules {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokens = new CookieCsrfTokenRepository();
        csrfTokens.setCookieName("XSRF-TOKEN");
        csrfTokens.setHeaderName("X-CSRF-TOKEN");

        SimpleUrlAuthenticationSuccessHandler success = new SimpleUrlAuthenticationSuccessHandler("/admin");
        SimpleUrlAuthenticationFailureHandler failure =
                new SimpleUrlAuthenticationFailureHandler("/admin/login?error=1");

        // APIs answer 401 instead of redirecting to a login page that only
        // exists once the Phase 5b server-rendered pages land.
        var apiEntryPoint = new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                org.springframework.http.HttpStatus.UNAUTHORIZED);
        var apiMatcher = new org.springframework.security.web.util.matcher.OrRequestMatcher(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/staking/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/rewards/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/faucet/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/internal/**"));

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokens)
                        // /api/cart mirrors [IgnoreAntiforgeryToken]: public storefront JS
                        // calls it without a token.
                        .ignoringRequestMatchers("/admin/logout", "/api/cart/**"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(apiEntryPoint, apiMatcher))
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
}

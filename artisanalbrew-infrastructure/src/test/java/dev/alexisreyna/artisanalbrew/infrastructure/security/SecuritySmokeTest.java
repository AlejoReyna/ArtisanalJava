package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The security composition boots inside the application context: filter
 * chain, V3-aware password encoder, and seeder wiring. Endpoint behavior
 * (redirects, wallet sessions) is exercised with the Phase 5 controllers.
 */
@SpringBootTest(classes = TestApplication.class)
class SecuritySmokeTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void securityBeansAreWired() {
        assertThat(context.getBean(SecurityFilterChain.class)).isNotNull();
        assertThat(context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class))
                .isInstanceOf(AspNetIdentityPasswordEncoder.class);
        assertThat(context.getBean(JpaAdminIdentitySeeder.class)).isNotNull();
        assertThat(context.getBean(DefaultEvmWalletChallengeService.class)).isNotNull();
    }
}

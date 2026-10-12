package dev.alexisreyna.artisanalbrew.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The V3 vector below was produced outside this codebase (Python
 * {@code hashlib.pbkdf2_hmac}, password {@code Test1234!}), so the test
 * verifies format parsing — not JCA against itself.
 */
class AspNetIdentityPasswordEncoderTest {

    private static final String V3_VECTOR =
            "AQAAAAEAACcQAAAAEAABAgMEBQYHCAkKCwwNDg9g+bkb2Yg4UHSIbucgAYUbAe4MXXGWIQZM0D5vZOqNEQ==";

    private final AspNetIdentityPasswordEncoder encoder = new AspNetIdentityPasswordEncoder();

    @Test
    void verifiesIdentityV3Hashes() {
        assertThat(encoder.matches("Test1234!", V3_VECTOR)).isTrue();
        assertThat(encoder.matches("test1234!", V3_VECTOR)).isFalse();
        assertThat(encoder.matches("", V3_VECTOR)).isFalse();
        assertThat(encoder.needsUpgrade(V3_VECTOR)).isTrue();
    }

    @Test
    void rejectsMalformedInput() {
        assertThat(encoder.matches("x", "not-base64!!!")).isFalse();
        assertThat(encoder.matches("x", null)).isFalse();
        assertThat(encoder.matches("x", java.util.Base64.getEncoder().encodeToString(new byte[]{2, 3}))).isFalse();
    }

    @Test
    void bcryptRoundTripForNewPasswords() {
        String hash = encoder.encode("NewPassword1");
        assertThat(hash).startsWith("$2");
        assertThat(encoder.matches("NewPassword1", hash)).isTrue();
        assertThat(encoder.matches("WrongPassword1", hash)).isFalse();
        assertThat(encoder.needsUpgrade(hash)).isFalse();
    }
}

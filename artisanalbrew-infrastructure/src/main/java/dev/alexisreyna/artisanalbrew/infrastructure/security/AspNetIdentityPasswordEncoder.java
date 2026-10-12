package dev.alexisreyna.artisanalbrew.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Verifies ASP.NET Identity password hashes while issuing BCrypt for new ones.
 *
 * <p>Identity V3 layout (base64): {@code 0x01 | prf BE32 | iter BE32 | saltLen BE32 |
 * salt | subkey} with PRF=HMACSHA256, 10 000 iterations, 128-bit salt,
 * 256-bit subkey. Without this, every password stored by the .NET app would
 * silently stop working after the port — the kind of migration break that only
 * shows up on login day.</p>
 */
public final class AspNetIdentityPasswordEncoder implements PasswordEncoder {
    private static final byte FORMAT_V3 = 0x01;
    private static final int PRF_HMAC_SHA256 = 1;
    private static final int SALT_BITS = 128;
    private static final int SUBKEY_BITS = 256;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedPassword);
            if (isIdentityV3(decoded)) {
                return verifyV3(rawPassword.toString(), decoded);
            }
        } catch (IllegalArgumentException ignored) {
            // Not base64 (BCrypt hashes contain '$') — falls through below.
        }
        return bcrypt.matches(rawPassword, encodedPassword);
    }

    /** True once this hash should be re-issued as BCrypt (i.e. it is a legacy V3 hash). */
    public boolean needsUpgrade(String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        try {
            return isIdentityV3(Base64.getDecoder().decode(encodedPassword));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean isIdentityV3(byte[] decoded) {
        return decoded.length > 13 && decoded[0] == FORMAT_V3;
    }

    static boolean verifyV3(String password, byte[] decoded) {
        try {
            int prf = readBigEndian(decoded, 1);
            int iterCount = readBigEndian(decoded, 5);
            int saltLength = readBigEndian(decoded, 9);
            if (prf != PRF_HMAC_SHA256 || iterCount < 1 || saltLength * 8 != SALT_BITS
                    || decoded.length < 13 + saltLength + SUBKEY_BITS / 8) {
                return false;
            }
            byte[] salt = new byte[saltLength];
            System.arraycopy(decoded, 13, salt, 0, saltLength);
            byte[] expected = new byte[SUBKEY_BITS / 8];
            System.arraycopy(decoded, 13 + saltLength, expected, 0, expected.length);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterCount, SUBKEY_BITS);
            byte[] actual = factory.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }

    private static int readBigEndian(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
    }
}

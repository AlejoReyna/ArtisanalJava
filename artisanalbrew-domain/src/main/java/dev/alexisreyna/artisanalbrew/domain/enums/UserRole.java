package dev.alexisreyna.artisanalbrew.domain.enums;

/** Port of {@code ThisCafeteria.Domain/Enums/UserRole.cs}. */
public enum UserRole {
    CUSTOMER(0),
    ADMIN(1);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole fromCode(int code) {
        for (UserRole r : values()) {
            if (r.code == code) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown UserRole code: " + code);
    }
}

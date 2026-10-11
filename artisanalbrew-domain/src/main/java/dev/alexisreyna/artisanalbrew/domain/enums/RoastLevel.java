package dev.alexisreyna.artisanalbrew.domain.enums;

/** Port of {@code ThisCafeteria.Domain/Enums/RoastLevel.cs}. */
public enum RoastLevel {
    LIGHT(0),
    MEDIUM(1),
    DARK(2);

    private final int code;

    RoastLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RoastLevel fromCode(int code) {
        for (RoastLevel r : values()) {
            if (r.code == code) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown RoastLevel code: " + code);
    }
}

package dev.alexisreyna.artisanalbrew.domain.enums;

/** Port of {@code ThisCafeteria.Domain/Enums/CoffeeProcess.cs}. */
public enum CoffeeProcess {
    WASHED(0),
    NATURAL(1),
    HONEY(2);

    private final int code;

    CoffeeProcess(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CoffeeProcess fromCode(int code) {
        for (CoffeeProcess p : values()) {
            if (p.code == code) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown CoffeeProcess code: " + code);
    }
}

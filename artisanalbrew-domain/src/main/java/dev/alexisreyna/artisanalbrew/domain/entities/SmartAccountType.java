package dev.alexisreyna.artisanalbrew.domain.entities;

/** Port of {@code SmartAccountType} in {@code SmartAccountRecord.cs}. Codes match C#. */
public enum SmartAccountType {
    SIMPLE_ACCOUNT(0),
    MODULAR_HYBRID_DELEGATOR(1);

    private final int code;

    SmartAccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static SmartAccountType fromCode(int code) {
        for (SmartAccountType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown SmartAccountType code: " + code);
    }
}

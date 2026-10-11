package dev.alexisreyna.artisanalbrew.domain.enums;

/** Port of {@code ThisCafeteria.Domain/Enums/OrderStatus.cs}. Codes match C# int values. */
public enum OrderStatus {
    PENDING(0),
    PROCESSING(1),
    COMPLETED(2),
    CANCELLED(3);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus code: " + code);
    }
}

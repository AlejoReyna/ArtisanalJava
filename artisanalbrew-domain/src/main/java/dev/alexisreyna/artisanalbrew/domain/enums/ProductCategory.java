package dev.alexisreyna.artisanalbrew.domain.enums;

/** Port of {@code ThisCafeteria.Domain/Enums/ProductCategory.cs}. */
public enum ProductCategory {
    BEANS(0),
    BREWING_EQUIPMENT(1),
    CERAMICS_AND_GOODS(2);

    private final int code;

    ProductCategory(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProductCategory fromCode(int code) {
        for (ProductCategory c : values()) {
            if (c.code == code) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown ProductCategory code: " + code);
    }
}

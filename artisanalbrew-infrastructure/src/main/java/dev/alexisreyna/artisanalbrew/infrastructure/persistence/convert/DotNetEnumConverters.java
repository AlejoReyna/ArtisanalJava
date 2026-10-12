package dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert;

import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;
import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * .NET stores these enums as their C# member names ({@code HasConversion<string>}).
 * Java member names differ (SCREAMING_SNAKE), so explicit converters preserve
 * the stored values — and therefore existing rows.
 */
public final class DotNetEnumConverters {
    private DotNetEnumConverters() {
    }

    @Converter(autoApply = false)
    public static final class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
        @Override
        public String convertToDatabaseColumn(OrderStatus status) {
            if (status == null) {
                return null;
            }
            return switch (status) {
                case PENDING -> "Pending";
                case PROCESSING -> "Processing";
                case COMPLETED -> "Completed";
                case CANCELLED -> "Cancelled";
            };
        }

        @Override
        public OrderStatus convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return switch (dbData) {
                case "Pending" -> OrderStatus.PENDING;
                case "Processing" -> OrderStatus.PROCESSING;
                case "Completed" -> OrderStatus.COMPLETED;
                case "Cancelled" -> OrderStatus.CANCELLED;
                default -> throw new IllegalArgumentException("Unknown OrderStatus: " + dbData);
            };
        }
    }

    @Converter(autoApply = false)
    public static final class UserRoleConverter implements AttributeConverter<UserRole, String> {
        @Override
        public String convertToDatabaseColumn(UserRole role) {
            if (role == null) {
                return null;
            }
            return switch (role) {
                case CUSTOMER -> "Customer";
                case ADMIN -> "Admin";
            };
        }

        @Override
        public UserRole convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return switch (dbData) {
                case "Customer" -> UserRole.CUSTOMER;
                case "Admin" -> UserRole.ADMIN;
                default -> throw new IllegalArgumentException("Unknown UserRole: " + dbData);
            };
        }
    }

    @Converter(autoApply = false)
    public static final class ProductCategoryConverter implements AttributeConverter<ProductCategory, String> {
        @Override
        public String convertToDatabaseColumn(ProductCategory category) {
            if (category == null) {
                return null;
            }
            return switch (category) {
                case BEANS -> "Beans";
                case BREWING_EQUIPMENT -> "BrewingEquipment";
                case CERAMICS_AND_GOODS -> "CeramicsAndGoods";
            };
        }

        @Override
        public ProductCategory convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return switch (dbData) {
                case "Beans" -> ProductCategory.BEANS;
                case "BrewingEquipment" -> ProductCategory.BREWING_EQUIPMENT;
                case "CeramicsAndGoods" -> ProductCategory.CERAMICS_AND_GOODS;
                default -> throw new IllegalArgumentException("Unknown ProductCategory: " + dbData);
            };
        }
    }

    @Converter(autoApply = false)
    public static final class RoastLevelConverter implements AttributeConverter<RoastLevel, String> {
        @Override
        public String convertToDatabaseColumn(RoastLevel level) {
            if (level == null) {
                return null;
            }
            return switch (level) {
                case LIGHT -> "Light";
                case MEDIUM -> "Medium";
                case DARK -> "Dark";
            };
        }

        @Override
        public RoastLevel convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return switch (dbData) {
                case "Light" -> RoastLevel.LIGHT;
                case "Medium" -> RoastLevel.MEDIUM;
                case "Dark" -> RoastLevel.DARK;
                default -> throw new IllegalArgumentException("Unknown RoastLevel: " + dbData);
            };
        }
    }

    @Converter(autoApply = false)
    public static final class CoffeeProcessConverter implements AttributeConverter<CoffeeProcess, String> {
        @Override
        public String convertToDatabaseColumn(CoffeeProcess process) {
            if (process == null) {
                return null;
            }
            return switch (process) {
                case WASHED -> "Washed";
                case NATURAL -> "Natural";
                case HONEY -> "Honey";
            };
        }

        @Override
        public CoffeeProcess convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return switch (dbData) {
                case "Washed" -> CoffeeProcess.WASHED;
                case "Natural" -> CoffeeProcess.NATURAL;
                case "Honey" -> CoffeeProcess.HONEY;
                default -> throw new IllegalArgumentException("Unknown CoffeeProcess: " + dbData);
            };
        }
    }
}

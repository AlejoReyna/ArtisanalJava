package dev.alexisreyna.artisanalbrew.web.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.alexisreyna.artisanalbrew.domain.enums.CoffeeProcess;
import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import dev.alexisreyna.artisanalbrew.domain.enums.RoastLevel;
import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ASP.NET serializes enums as their C# int codes by default; Jackson would
 * emit {@code "PENDING"}. This module keeps the wire format identical both
 * ways without touching the framework-free domain module.
 */
@Component
public class DotNetEnumJacksonModule extends SimpleModule {

    public DotNetEnumJacksonModule() {
        super("DotNetEnums");
        addSerializer(OrderStatus.class, new CodeSerializer<>(OrderStatus::getCode));
        addDeserializer(OrderStatus.class, new CodeDeserializer<>(OrderStatus::fromCode));
        addSerializer(ProductCategory.class, new CodeSerializer<>(ProductCategory::getCode));
        addDeserializer(ProductCategory.class, new CodeDeserializer<>(ProductCategory::fromCode));
        addSerializer(RoastLevel.class, new CodeSerializer<>(RoastLevel::getCode));
        addDeserializer(RoastLevel.class, new CodeDeserializer<>(RoastLevel::fromCode));
        addSerializer(CoffeeProcess.class, new CodeSerializer<>(CoffeeProcess::getCode));
        addDeserializer(CoffeeProcess.class, new CodeDeserializer<>(CoffeeProcess::fromCode));
        addSerializer(UserRole.class, new CodeSerializer<>(UserRole::getCode));
        addDeserializer(UserRole.class, new CodeDeserializer<>(UserRole::fromCode));
    }

    private static final class CodeSerializer<T extends Enum<T>> extends JsonSerializer<T> {
        private final java.util.function.ToIntFunction<T> codes;

        CodeSerializer(java.util.function.ToIntFunction<T> codes) {
            this.codes = codes;
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(codes.applyAsInt(value));
        }
    }

    private static final class CodeDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {
        private final java.util.function.IntFunction<T> fromCode;

        CodeDeserializer(java.util.function.IntFunction<T> fromCode) {
            this.fromCode = fromCode;
        }

        @Override
        public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return fromCode.apply(parser.getIntValue());
        }
    }
}

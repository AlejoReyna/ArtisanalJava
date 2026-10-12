package dev.alexisreyna.artisanalbrew.web.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.domain.enums.OrderStatus;
import dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DotNetEnumJacksonModuleTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new DotNetEnumJacksonModule());

    record Enums(OrderStatus status, ProductCategory category) {
    }

    @Test
    void enumsTravelAsCodes() throws Exception {
        String json = mapper.writeValueAsString(new Enums(OrderStatus.PROCESSING, ProductCategory.BEANS));
        assertThat(json).isEqualTo("{\"status\":1,\"category\":0}");
        Enums back = mapper.readValue("{\"status\":2,\"category\":1}", Enums.class);
        assertThat(back.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(back.category()).isEqualTo(ProductCategory.BREWING_EQUIPMENT);
    }
}

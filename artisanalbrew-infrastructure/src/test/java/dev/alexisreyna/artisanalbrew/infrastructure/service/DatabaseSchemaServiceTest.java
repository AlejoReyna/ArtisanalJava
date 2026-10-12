package dev.alexisreyna.artisanalbrew.infrastructure.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.TestApplication;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
class DatabaseSchemaServiceTest {

    @Autowired
    private DatabaseSchemaServiceImpl schemas;

    @Test
    void schemaDescribesLiveModel() {
        var schema = schemas.getSchema(true);
        // 22 mapped entities minus ApplicationUser/Role/UserRole (withheld Identity tables).
        assertThat(schema.tableCount()).isEqualTo(19);
        assertThat(schema.tables()).anySatisfy(t -> {
            assertThat(t.entity()).isEqualTo("Order");
            assertThat(t.table()).isEqualTo("Orders");
            assertThat(t.group()).isEqualTo("Commerce");
        });
        assertThat(schema.tables()).noneSatisfy(t ->
                assertThat(t.table()).startsWith("AspNet"));
        var cached = schemas.getSchema(true);
        assertThat(cached).isSameAs(schema);
        var nocounts = schemas.getSchema(false);
        assertThat(nocounts.totalRows()).isEqualTo(0);
    }
}

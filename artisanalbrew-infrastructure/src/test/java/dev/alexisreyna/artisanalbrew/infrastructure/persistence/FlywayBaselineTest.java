package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.ProductJpaRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V1 baseline applies cleanly (proves H2/PostgreSQL-portable SQL) and the
 * seed catalog from {@code SeedData} is present.
 */
@SpringBootTest(classes = TestApplication.class)
class FlywayBaselineTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private ProductJpaRepository products;

    @Test
    void baselineMigratesAndSeedsCatalog() {
        assertThat(flyway.info().applied()).anySatisfy(m -> assertThat(m.getVersion().toString()).isEqualTo("1"));
        assertThat(products.count()).isEqualTo(3);
        assertThat(products.findBySlug("house-espresso")).isPresent();
        assertThat(products.findBySlug("house-espresso").orElseThrow().category)
                .isEqualTo(dev.alexisreyna.artisanalbrew.domain.enums.ProductCategory.BEANS);
    }
}

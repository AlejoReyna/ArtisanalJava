package dev.alexisreyna.artisanalbrew.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The H2 twin of V1 must stay byte-identical to the PostgreSQL V1 except for
 * the dropped partial-index WHERE clauses (H2 has no partial indexes).
 * Both files are generated from the EF snapshot by the same script; this test
 * guards future hand-edits.
 */
class MigrationParityTest {

    private static List<String> lines(String resource) throws Exception {
        try (var in = MigrationParityTest.class.getResourceAsStream(resource)) {
            assertThat(in).as("missing migration resource %s", resource).isNotNull();
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.stream(text.split("\n"))
                    .map(l -> l.contains("--") ? l.substring(0, l.indexOf("--")) : l)
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .toList();
        }
    }

    @Test
    void h2TwinMatchesPostgresBaseline() throws Exception {
        List<String> pg = lines("/db/migration/V1__baseline.sql");
        List<String> h2 = lines("/db/migration-h2/V1__baseline.sql");
        assertThat(h2).hasSameSizeAs(pg);
        for (int i = 0; i < pg.size(); i++) {
            String expected = pg.get(i);
            if (expected.startsWith("CREATE ") && expected.contains(" WHERE ")) {
                expected = expected.substring(0, expected.indexOf(" WHERE ")) + ";";
            }
            assertThat(h2.get(i)).as("line %d", i + 1).isEqualTo(expected);
        }
    }
}

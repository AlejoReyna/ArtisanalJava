package dev.alexisreyna.artisanalbrew.web.api;

import dev.alexisreyna.artisanalbrew.application.dto.DatabaseSchemaDto;
import dev.alexisreyna.artisanalbrew.application.service.DatabaseSchemaService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Port of {@code TransparencyController} ({@code Web/Controllers}):
 * structure and headline totals only, cached for 60 seconds.
 */
@RestController
@RequestMapping("/api/transparency")
public class TransparencyController {
    private final DatabaseSchemaService schemas;

    public TransparencyController(DatabaseSchemaService schemas) {
        this.schemas = schemas;
    }

    @GetMapping("/schema")
    public ResponseEntity<DatabaseSchemaDto> schema(
            @RequestParam(defaultValue = "true") boolean counts) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(schemas.getSchema(counts));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        DatabaseSchemaDto schema = schemas.getSchema(true);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("generatedAtUtc", schema.generatedAtUtc());
        body.put("provider", schema.provider());
        body.put("tableCount", schema.tableCount());
        body.put("columnCount", schema.columnCount());
        body.put("relationshipCount", schema.relationshipCount());
        body.put("totalRows", schema.totalRows());
        var groups = schema.tables().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.group() == null ? "" : t.group(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));
        List<Map<String, Object>> groupRows = groups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("group", entry.getKey());
                    row.put("tables", entry.getValue().size());
                    row.put("rows", entry.getValue().stream()
                            .mapToLong(t -> t.rows() == null ? 0 : t.rows()).sum());
                    return row;
                })
                .toList();
        body.put("groups", groupRows);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(body);
    }
}

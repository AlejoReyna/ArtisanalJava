package dev.alexisreyna.artisanalbrew.application.dto;

import java.util.List;

/**
 * Port of {@code DTOs/DatabaseSchemaDto.cs}. Structure only — no row contents.
 */
public record DatabaseSchemaDto(
        String generatedAtUtc,
        String provider,
        int tableCount,
        int columnCount,
        int relationshipCount,
        long totalRows,
        List<String> notes,
        List<SchemaTableDto> tables) {
}

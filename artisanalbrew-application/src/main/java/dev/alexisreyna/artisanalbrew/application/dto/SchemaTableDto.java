package dev.alexisreyna.artisanalbrew.application.dto;

import java.util.List;

/** Port of {@code SchemaTableDto} in {@code DTOs/DatabaseSchemaDto.cs}. */
public record SchemaTableDto(
        String entity,
        String table,
        String group,
        Long rows,
        List<SchemaColumnDto> columns,
        List<SchemaRelationshipDto> references) {
}

package dev.alexisreyna.artisanalbrew.application.dto;

/** Port of {@code SchemaRelationshipDto} in {@code DTOs/DatabaseSchemaDto.cs}. */
public record SchemaRelationshipDto(String fromColumn, String toTable, String toColumn, boolean required) {
}

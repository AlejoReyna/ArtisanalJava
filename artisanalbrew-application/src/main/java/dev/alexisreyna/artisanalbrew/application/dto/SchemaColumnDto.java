package dev.alexisreyna.artisanalbrew.application.dto;

/** Port of {@code SchemaColumnDto} in {@code DTOs/DatabaseSchemaDto.cs}. */
public record SchemaColumnDto(
        String name,
        String type,
        boolean nullable,
        boolean primaryKey,
        boolean foreignKey,
        Integer maxLength) {
}

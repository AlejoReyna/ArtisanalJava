package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.DatabaseSchemaDto;

/** Port of {@code Services/IDatabaseSchemaService.cs}. */
public interface DatabaseSchemaService {
    DatabaseSchemaDto getSchema(boolean includeCounts);

    default DatabaseSchemaDto getSchema() {
        return getSchema(true);
    }
}

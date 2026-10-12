package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import dev.alexisreyna.artisanalbrew.domain.enums.UserRole;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert.DotNetEnumConverters;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA row for {@code UserProfiles}. The owned {@code Avatar} maps to one TEXT
 * column holding the same PascalCase JSON object .NET stores as jsonb.
 */
@Entity
@Table(name = "\"UserProfiles\"")
public class UserProfileEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"Email\"")
    public String email;

    @Column(name = "\"DisplayName\"")
    public String displayName;

    @Column(name = "\"Role\"")
    @Convert(converter = DotNetEnumConverters.UserRoleConverter.class)
    public UserRole role;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"Avatar\"")
    public String avatarJson;
}

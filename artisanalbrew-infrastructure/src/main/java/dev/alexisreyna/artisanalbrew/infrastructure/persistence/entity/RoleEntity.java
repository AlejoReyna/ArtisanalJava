package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** JPA row for {@code AspNetRoles}. */
@Entity
@Table(name = "\"AspNetRoles\"")
public class RoleEntity {
    @Id
    @Column(name = "\"Id\"")
    public String id;

    @Column(name = "\"Name\"")
    public String name;

    @Column(name = "\"NormalizedName\"")
    public String normalizedName;

    @Column(name = "\"ConcurrencyStamp\"")
    public String concurrencyStamp;
}

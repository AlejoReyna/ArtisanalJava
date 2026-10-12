package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code Carts}. */
@Entity
@Table(name = "\"Carts\"")
public class CartEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"UserProfileId\"")
    public UUID userProfileId;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"UpdatedAt\"")
    public Instant updatedAt;
}

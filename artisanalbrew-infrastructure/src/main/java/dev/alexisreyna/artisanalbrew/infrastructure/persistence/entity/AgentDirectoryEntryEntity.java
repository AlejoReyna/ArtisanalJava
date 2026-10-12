package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code AgentDirectoryEntries}. */
@Entity
@Table(name = "\"AgentDirectoryEntries\"")
public class AgentDirectoryEntryEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"RegistryAddress\"")
    public String registryAddress;

    @Column(name = "\"AgentId\"")
    public long agentId;

    @Column(name = "\"OwnerAddress\"")
    public String ownerAddress;

    @Column(name = "\"MetadataUri\"")
    public String metadataUri;

    @Column(name = "\"IsActive\"")
    public boolean active;

    @Column(name = "\"CreatedAtUtc\"")
    public Instant createdAtUtc;

    @Column(name = "\"UpdatedAtUtc\"")
    public Instant updatedAtUtc;
}

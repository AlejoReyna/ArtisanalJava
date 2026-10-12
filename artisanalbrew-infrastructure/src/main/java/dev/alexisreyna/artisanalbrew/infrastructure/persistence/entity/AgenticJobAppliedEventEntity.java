package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code AgenticJobAppliedEvents}. */
@Entity
@Table(name = "\"AgenticJobAppliedEvents\"")
public class AgenticJobAppliedEventEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"ContractAddress\"")
    public String contractAddress;

    @Column(name = "\"TransactionHash\"")
    public String transactionHash;

    @Column(name = "\"LogIndex\"")
    public int logIndex;

    @Column(name = "\"AppliedAtUtc\"")
    public Instant appliedAtUtc;
}

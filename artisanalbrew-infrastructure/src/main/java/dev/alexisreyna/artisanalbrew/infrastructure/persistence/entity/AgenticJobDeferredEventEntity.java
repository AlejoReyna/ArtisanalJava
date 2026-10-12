package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code AgenticJobDeferredEvents}. */
@Entity
@Table(name = "\"AgenticJobDeferredEvents\"")
public class AgenticJobDeferredEventEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"ContractAddress\"")
    public String contractAddress;

    @Column(name = "\"OnChainJobId\"")
    public long onChainJobId;

    @Column(name = "\"EventType\"")
    public String eventType;

    @Column(name = "\"TransactionHash\"")
    public String transactionHash;

    @Column(name = "\"LogIndex\"")
    public int logIndex;

    @Column(name = "\"BlockNumber\"")
    public long blockNumber;

    @Column(name = "\"DeferralReason\"")
    public String deferralReason;

    @Column(name = "\"DeferredAtUtc\"")
    public Instant deferredAtUtc;
}

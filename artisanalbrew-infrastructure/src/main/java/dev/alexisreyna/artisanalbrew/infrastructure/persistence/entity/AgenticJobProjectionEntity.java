package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA row for {@code AgenticJobs}. The {@code ConcurrencyToken} stays a plain
 * column (not {@code @Version}): the applicator increments it manually exactly
 * like .NET, and Hibernate would otherwise increment it a second time.
 */
@Entity
@Table(name = "\"AgenticJobs\"")
public class AgenticJobProjectionEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"ChainKey\"")
    public String chainKey;

    @Column(name = "\"EscrowAddress\"")
    public String escrowAddress;

    @Column(name = "\"JobId\"")
    public long jobId;

    @Column(name = "\"ClientAddress\"")
    public String clientAddress;

    @Column(name = "\"ProviderAddress\"")
    public String providerAddress;

    @Column(name = "\"EvaluatorAddress\"")
    public String evaluatorAddress;

    @Column(name = "\"DescriptionCommitment\"")
    public String descriptionCommitment;

    @Column(name = "\"Budget\"")
    public BigDecimal budget;

    @Column(name = "\"ExpiredAt\"")
    public long expiredAt;

    @Column(name = "\"Status\"")
    public String status;

    @Column(name = "\"DeliverableCommitment\"")
    public String deliverableCommitment;

    @Column(name = "\"DecisionReason\"")
    public String decisionReason;

    @Column(name = "\"CreatedAtUtc\"")
    public Instant createdAtUtc;

    @Column(name = "\"UpdatedAtUtc\"")
    public Instant updatedAtUtc;

    @Column(name = "\"OnChainJobId\"")
    public long onChainJobId;

    @Column(name = "\"ChainId\"")
    public long chainId;

    @Column(name = "\"ContractAddress\"")
    public String contractAddress;

    @Column(name = "\"CreationTransactionHash\"")
    public String creationTransactionHash;

    @Column(name = "\"FundedTransactionHash\"")
    public String fundedTransactionHash;

    @Column(name = "\"CompletionTransactionHash\"")
    public String completionTransactionHash;

    @Column(name = "\"LastReconciledBlock\"")
    public long lastReconciledBlock;

    @Column(name = "\"ConcurrencyToken\"")
    public long concurrencyToken;
}

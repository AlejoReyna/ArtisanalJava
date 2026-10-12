package dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** JPA row for {@code TransparencyRecords}. */
@Entity
@Table(name = "\"TransparencyRecords\"")
public class TransparencyRecordEntity {
    @Id
    @Column(name = "\"Id\"")
    public UUID id;

    @Column(name = "\"OrderId\"")
    public UUID orderId;

    @Column(name = "\"OrderNumber\"")
    public String orderNumber;

    @Column(name = "\"ProductName\"")
    public String productName;

    @Column(name = "\"Quantity\"")
    public int quantity;

    @Column(name = "\"Total\"")
    public BigDecimal total;

    @Column(name = "\"OrderHash\"")
    public String orderHash;

    @Column(name = "\"ChainId\"")
    public int chainId;

    @Column(name = "\"NetworkName\"")
    public String networkName;

    @Column(name = "\"ContractAddress\"")
    public String contractAddress;

    @Column(name = "\"TransactionHash\"")
    public String transactionHash;

    @Column(name = "\"ExplorerUrl\"")
    public String explorerUrl;

    @Column(name = "\"Status\"")
    public String status;

    @Column(name = "\"CreatedAt\"")
    public Instant createdAt;

    @Column(name = "\"RecordedOnChainAt\"")
    public Instant recordedOnChainAt;
}

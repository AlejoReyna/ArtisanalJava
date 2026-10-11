package dev.alexisreyna.artisanalbrew.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Port of {@code ThisCafeteria.Domain/Entities/TransparencyRecord.cs}. */
public class TransparencyRecord {
    private UUID id = UUID.randomUUID();
    private UUID orderId;
    private String orderNumber = "";
    private String productName = "";
    private int quantity;
    private BigDecimal total = BigDecimal.ZERO;
    private String orderHash = "";
    private int chainId;
    private String networkName = "";
    private String contractAddress = "";
    private String transactionHash = "";
    private String explorerUrl = "";
    private String status = "Pending";
    private Instant createdAt = Instant.now();
    private Instant recordedOnChainAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getOrderHash() { return orderHash; }
    public void setOrderHash(String orderHash) { this.orderHash = orderHash; }
    public int getChainId() { return chainId; }
    public void setChainId(int chainId) { this.chainId = chainId; }
    public String getNetworkName() { return networkName; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
    public String getExplorerUrl() { return explorerUrl; }
    public void setExplorerUrl(String explorerUrl) { this.explorerUrl = explorerUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getRecordedOnChainAt() { return recordedOnChainAt; }
    public void setRecordedOnChainAt(Instant recordedOnChainAt) { this.recordedOnChainAt = recordedOnChainAt; }
}

package dev.alexisreyna.artisanalbrew.infrastructure.persistence.adapter;

import dev.alexisreyna.artisanalbrew.application.repository.TransparencyRecordRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.TransparencyRecordEntity;
import dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo.TransparencyRecordJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** JPA adapter for {@link TransparencyRecordRepository}. */
@Repository
@Transactional
public class JpaTransparencyRecordRepositoryAdapter implements TransparencyRecordRepository {
    private final TransparencyRecordJpaRepository jpa;

    public JpaTransparencyRecordRepositoryAdapter(TransparencyRecordJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void addRange(List<TransparencyRecord> records) {
        jpa.saveAll(records.stream().map(JpaTransparencyRecordRepositoryAdapter::toEntity).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransparencyRecord> getRecent(int count) {
        return jpa.findTop25ByOrderByCreatedAtDesc().stream().limit(count)
                .map(JpaTransparencyRecordRepositoryAdapter::toDomain).toList();
    }

    static TransparencyRecordEntity toEntity(TransparencyRecord record) {
        TransparencyRecordEntity e = new TransparencyRecordEntity();
        e.id = record.getId();
        e.orderId = record.getOrderId();
        e.orderNumber = record.getOrderNumber();
        e.productName = record.getProductName();
        e.quantity = record.getQuantity();
        e.total = record.getTotal();
        e.orderHash = record.getOrderHash();
        e.chainId = record.getChainId();
        e.networkName = record.getNetworkName();
        e.contractAddress = record.getContractAddress();
        e.transactionHash = record.getTransactionHash();
        e.explorerUrl = record.getExplorerUrl();
        e.status = record.getStatus();
        e.createdAt = record.getCreatedAt();
        e.recordedOnChainAt = record.getRecordedOnChainAt();
        return e;
    }

    static TransparencyRecord toDomain(TransparencyRecordEntity e) {
        TransparencyRecord record = new TransparencyRecord();
        record.setId(e.id);
        record.setOrderId(e.orderId);
        record.setOrderNumber(e.orderNumber);
        record.setProductName(e.productName);
        record.setQuantity(e.quantity);
        record.setTotal(e.total);
        record.setOrderHash(e.orderHash);
        record.setChainId(e.chainId);
        record.setNetworkName(e.networkName);
        record.setContractAddress(e.contractAddress);
        record.setTransactionHash(e.transactionHash);
        record.setExplorerUrl(e.explorerUrl);
        record.setStatus(e.status);
        record.setCreatedAt(e.createdAt);
        record.setRecordedOnChainAt(e.recordedOnChainAt);
        return record;
    }
}

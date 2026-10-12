package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.TransparencyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransparencyRecordJpaRepository extends JpaRepository<TransparencyRecordEntity, UUID> {
    List<TransparencyRecordEntity> findByOrderId(UUID orderId);

    List<TransparencyRecordEntity> findTop25ByOrderByCreatedAtDesc();

    void deleteByOrderId(UUID orderId);

    void deleteByOrderIdIn(List<UUID> orderIds);
}

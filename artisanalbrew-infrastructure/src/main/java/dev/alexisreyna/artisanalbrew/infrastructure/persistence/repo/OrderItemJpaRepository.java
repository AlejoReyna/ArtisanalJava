package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, UUID> {
    List<OrderItemEntity> findByOrderId(UUID orderId);

    void deleteByOrderId(UUID orderId);

    void deleteByOrderIdIn(List<UUID> orderIds);
}

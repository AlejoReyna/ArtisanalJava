package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    boolean existsByPaymentTransactionHash(String paymentTransactionHash);

    List<OrderEntity> findByUserProfileId(UUID userProfileId);

    void deleteByUserProfileId(UUID userProfileId);

    long countByUserProfileId(UUID userProfileId);
}

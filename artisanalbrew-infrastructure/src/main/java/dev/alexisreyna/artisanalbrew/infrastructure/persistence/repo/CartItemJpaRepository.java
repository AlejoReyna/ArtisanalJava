package dev.alexisreyna.artisanalbrew.infrastructure.persistence.repo;

import dev.alexisreyna.artisanalbrew.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, UUID> {
    List<CartItemEntity> findByCartId(UUID cartId);

    void deleteByCartId(UUID cartId);
}

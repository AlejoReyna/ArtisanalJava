package dev.alexisreyna.artisanalbrew.application.repository;

import dev.alexisreyna.artisanalbrew.domain.entities.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port of {@code Repositories/IOrderRepository.cs}. */
public interface OrderRepository {
    void add(Order order);

    boolean existsByPaymentHash(String paymentTransactionHash);

    Optional<Order> getById(UUID id);

    List<Order> getCommerceTransactions();

    List<Order> getOrdersForUser(UUID userProfileId);

    void delete(Order order);
}

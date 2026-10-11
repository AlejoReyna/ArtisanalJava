package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.CommerceTransactionDto;
import dev.alexisreyna.artisanalbrew.application.dto.CreateOrderRequest;
import dev.alexisreyna.artisanalbrew.application.dto.OrderDto;

import java.util.List;
import java.util.UUID;

/** Port of {@code Services/IOrderService.cs}. */
public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request, UUID authenticatedUserProfileId);

    List<CommerceTransactionDto> getCommerceTransactions();

    List<OrderDto> getOrdersForUser(UUID userProfileId);

    boolean deleteOrder(UUID id);
}

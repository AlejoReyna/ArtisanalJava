package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.OrderDetails;

/** Port of {@code Services/IReceiptService.cs}. */
public interface ReceiptService {
    void sendReceipt(OrderDetails order);
}

package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.dto.TransparencyRecordDto;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;

import java.util.List;

/** Port of {@code Services/ITransparencyService.cs}. */
public interface TransparencyService {
    void createPendingRecordsForOrder(Order order);

    List<TransparencyRecordDto> getRecentPurchases(int count);

    default List<TransparencyRecordDto> getRecentPurchases() {
        return getRecentPurchases(25);
    }
}

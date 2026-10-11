package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.repository.TransparencyRecordRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderItem;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderPricing;
import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultTransparencyServiceTest {

    static final class FakeRecords implements TransparencyRecordRepository {
        final List<TransparencyRecord> rows = new ArrayList<>();

        @Override
        public void addRange(List<TransparencyRecord> records) {
            rows.addAll(records);
        }

        @Override
        public List<TransparencyRecord> getRecent(int count) {
            return rows.stream().limit(count).toList();
        }
    }

    private static Order order(String number, BigDecimal total, List<OrderItem> items) {
        OrderItem first = items.get(0);
        Order placed = Order.place(UUID.randomUUID(), number, "0xabc", items,
                new OrderPricing(first.getUnitPrice(), new BigDecimal("4.00"), BigDecimal.ZERO,
                        first.getUnitPrice().add(new BigDecimal("4.00")), BigDecimal.ZERO, total),
                null);
        return placed;
    }

    private static OrderItem item(String name, int qty, String price) {
        OrderItem item = new OrderItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(name);
        item.setQuantity(qty);
        item.setUnitPrice(new BigDecimal(price));
        item.setTotal(new BigDecimal(price).multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    @Test
    void hashIsDeterministicAndItemOrderIndependent() {
        List<OrderItem> items = List.of(item("Espresso", 2, "10.00"), item("Latte", 1, "5.00"));
        List<OrderItem> reversed = new ArrayList<>(items);
        Collections.reverse(reversed);
        String a = DefaultTransparencyService.createOrderHash(order("ORD-1", new BigDecimal("29.00"), items));
        String b = DefaultTransparencyService.createOrderHash(order("ORD-1", new BigDecimal("29.00"), reversed));
        assertThat(a).startsWith("0x").hasSize(66);
        assertThat(a).isEqualTo(b);
        String different = DefaultTransparencyService.createOrderHash(order("ORD-1", new BigDecimal("30.00"), items));
        assertThat(different).isNotEqualTo(a);
    }

    @Test
    void emptyOrdersProduceNoRecords() {
        FakeRecords records = new FakeRecords();
        DefaultTransparencyService service =
                new DefaultTransparencyService(records, new BlockchainNetworkOptions());
        Order placed = Order.place(UUID.randomUUID(), "ORD-EMPTY", "0xabc", List.of(),
                new OrderPricing(BigDecimal.ZERO, new BigDecimal("4.00"), BigDecimal.ZERO,
                        new BigDecimal("4.00"), BigDecimal.ZERO, new BigDecimal("4.00")),
                null);
        service.createPendingRecordsForOrder(placed);
        assertThat(records.rows).isEmpty();
    }
}

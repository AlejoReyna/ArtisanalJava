package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.application.config.BlockchainNetworkOptions;
import dev.alexisreyna.artisanalbrew.application.dto.TransparencyRecordDto;
import dev.alexisreyna.artisanalbrew.application.repository.TransparencyRecordRepository;
import dev.alexisreyna.artisanalbrew.domain.entities.Order;
import dev.alexisreyna.artisanalbrew.domain.entities.OrderItem;
import dev.alexisreyna.artisanalbrew.domain.entities.TransparencyRecord;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Port of {@code TransparencyService} in {@code Services/TransparencyService.cs}.
 * Decimal formatting uses {@code toPlainString} (InvariantCulture equivalent).
 */
public final class DefaultTransparencyService implements TransparencyService {
    private final TransparencyRecordRepository records;
    private final BlockchainNetworkOptions chain;

    public DefaultTransparencyService(TransparencyRecordRepository records, BlockchainNetworkOptions chain) {
        this.records = records;
        this.chain = chain;
    }

    @Override
    public void createPendingRecordsForOrder(Order order) {
        if (order.getItems().isEmpty()) {
            return;
        }
        String orderHash = createOrderHash(order);
        List<TransparencyRecord> rows = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            TransparencyRecord record = new TransparencyRecord();
            record.setOrderId(order.getId());
            record.setOrderNumber(order.getOrderNumber());
            record.setProductName(item.getProductName());
            record.setQuantity(item.getQuantity());
            record.setTotal(item.getTotal());
            record.setOrderHash(orderHash);
            record.setChainId(chain.getChainId());
            record.setNetworkName(chain.getNetworkName());
            record.setStatus("Pending");
            record.setCreatedAt(java.time.Instant.now());
            rows.add(record);
        }
        records.addRange(rows);
        order.getTransparencyRecords().addAll(rows);
    }

    @Override
    public List<TransparencyRecordDto> getRecentPurchases(int count) {
        return records.getRecent(count).stream().map(DefaultTransparencyService::map).toList();
    }

    static String createOrderHash(Order order) {
        String items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getProductName))
                .map(item -> item.getProductId() + ":" + item.getProductName() + ":"
                        + item.getQuantity() + ":" + plain(item.getUnitPrice()) + ":" + plain(item.getTotal()))
                .collect(Collectors.joining(";"));
        String payload = order.getOrderNumber() + "|" + plain(order.getTotal()) + "|" + items;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.UTF_8));
            return "0x" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String plain(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private static TransparencyRecordDto map(TransparencyRecord record) {
        return new TransparencyRecordDto(
                record.getId(), record.getOrderId(), record.getOrderNumber(),
                record.getProductName(), record.getQuantity(), record.getTotal(),
                record.getOrderHash(), record.getChainId(), record.getNetworkName(),
                record.getContractAddress(), record.getTransactionHash(), record.getExplorerUrl(),
                record.getStatus(), record.getCreatedAt(), record.getRecordedOnChainAt());
    }
}

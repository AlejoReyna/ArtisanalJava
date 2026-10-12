package dev.alexisreyna.artisanalbrew.infrastructure.chain.evm;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Decodes {@code Transfer} / ERC-4626 {@code Deposit} / {@code Withdraw}
 * receipt logs from web3j {@link Log} objects (mirrors the Nethereum
 * {@code IEventDTO} decoders: indexed addresses from topics, values from data).
 */
public final class EvmLogEvents {
    private static final String TRANSFER_TOPIC = topic("Transfer(address,address,uint256)");
    private static final String DEPOSIT_TOPIC = topic("Deposit(address,address,uint256,uint256)");
    private static final String WITHDRAW_TOPIC = topic("Withdraw(address,address,address,uint256,uint256)");

    private EvmLogEvents() {
    }

    public record Transfer(String contract, String from, String to, BigInteger value, int logIndex) {
    }

    public record Deposit(String contract, String receiver, BigInteger assets, BigInteger shares, int logIndex) {
    }

    public record Withdrawal(String contract, String receiver, BigInteger assets, BigInteger shares, int logIndex) {
    }

    public static Optional<Transfer> transfer(Log log) {
        if (!isTopic(log, TRANSFER_TOPIC) || log.getTopics().size() < 3) {
            return Optional.empty();
        }
        Optional<BigInteger> value = uintData(log);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Transfer(log.getAddress(), topicAddress(log, 1), topicAddress(log, 2),
                value.get(), logIndex(log)));
    }

    public static Optional<Deposit> deposit(Log log) {
        if (!isTopic(log, DEPOSIT_TOPIC) || log.getTopics().size() < 3) {
            return Optional.empty();
        }
        List<BigInteger> words = dataWords(log, 2);
        if (words == null) {
            return Optional.empty();
        }
        return Optional.of(new Deposit(log.getAddress(), topicAddress(log, 2),
                words.get(0), words.get(1), logIndex(log)));
    }

    public static Optional<Withdrawal> withdraw(Log log) {
        if (!isTopic(log, WITHDRAW_TOPIC) || log.getTopics().size() < 4) {
            return Optional.empty();
        }
        List<BigInteger> words = dataWords(log, 2);
        if (words == null) {
            return Optional.empty();
        }
        return Optional.of(new Withdrawal(log.getAddress(), topicAddress(log, 2),
                words.get(0), words.get(1), logIndex(log)));
    }

    static String topic(String signature) {
        byte[] hash = StakingCalldataDecoder.keccak256(
                signature.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "0x" + HexFormat.of().formatHex(hash);
    }

    private static boolean isTopic(Log log, String expected) {
        return log.getTopics() != null && !log.getTopics().isEmpty()
                && expected.equalsIgnoreCase(log.getTopics().get(0));
    }

    private static String topicAddress(Log log, int index) {
        String topic = log.getTopics().get(index);
        String hex = topic.startsWith("0x") ? topic.substring(2) : topic;
        return "0x" + hex.substring(Math.max(0, hex.length() - 40));
    }

    private static Optional<BigInteger> uintData(Log log) {
        List<BigInteger> words = dataWords(log, 1);
        return words == null ? Optional.empty() : Optional.of(words.get(0));
    }

    private static List<BigInteger> dataWords(Log log, int count) {
        String data = log.getData();
        if (data == null) {
            return null;
        }
        String hex = data.startsWith("0x") ? data.substring(2) : data;
        if (hex.length() < count * 64) {
            return null;
        }
        try {
            ArrayList<BigInteger> words = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                words.add(new BigInteger(hex.substring(i * 64, (i + 1) * 64), 16));
            }
            return words;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int logIndex(Log log) {
        try {
            Object raw = log.getLogIndex();
            if (raw instanceof BigInteger big) {
                return big.intValue();
            }
            if (raw instanceof Number number) {
                return number.intValue();
            }
            if (raw instanceof String text) {
                return EvmRpcClient.parseQuantity(text).intValue();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

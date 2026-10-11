package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of {@code Blockchain/SolanaAnchorEventCodec.cs}.
 * C# {@code JsonElement} log array → {@code List<String>} (no JSON dependency in
 * the application layer). Discriminators: {@code SHA256("event:" + name)[0..8]}.
 */
public final class SolanaAnchorEventCodec {
    public static final String DEPOSIT = "DepositEvent";
    public static final String REDEEM = "RedeemEvent";
    public static final String REWARD_CLAIMED = "RewardClaimedEvent";
    public static final String REWARD_FUNDED = "RewardFundedEvent";
    public static final String TRANSFER_CHECKPOINT = "TransferCheckpointEvent";

    private SolanaAnchorEventCodec() {
    }

    public static List<SolanaAnchorEvent> decode(List<String> logMessages, String trustedProgram, String... acceptedNames) {
        if (trustedProgram == null || trustedProgram.isBlank()) {
            throw new IllegalArgumentException("A trusted Solana program is required.");
        }
        Map<String, byte[]> discriminators = new LinkedHashMap<>();
        for (String name : acceptedNames) {
            discriminators.put(name, eventDiscriminator(name));
        }
        List<SolanaAnchorEvent> result = new ArrayList<>();
        Deque<String> invocationStack = new ArrayDeque<>();
        int logIndex = 0;
        for (String value : logMessages) {
            if (value == null) {
                value = "";
            }
            String invoked = tryInvocation(value);
            if (invoked != null) {
                invocationStack.push(invoked);
            } else if (value.startsWith("Program data: ")
                    && !invocationStack.isEmpty()
                    && invocationStack.peek().equals(trustedProgram)) {
                try {
                    byte[] buffer = Base64.getDecoder().decode(value.substring(14));
                    for (Map.Entry<String, byte[]> e : discriminators.entrySet()) {
                        if (startsWith(buffer, e.getValue())) {
                            result.add(new SolanaAnchorEvent(e.getKey(), logIndex,
                                    Arrays.copyOfRange(buffer, 8, buffer.length)));
                            break;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                    // Ignore unrelated or malformed program log output.
                }
            } else {
                String completed = tryCompletion(value);
                if (completed != null && !invocationStack.isEmpty() && invocationStack.peek().equals(completed)) {
                    invocationStack.pop();
                }
            }
            logIndex++;
        }
        return result;
    }

    private static String tryInvocation(String value) {
        String prefix = "Program ";
        String marker = " invoke [";
        if (!value.startsWith(prefix)) {
            return null;
        }
        int markerIndex = value.indexOf(marker, prefix.length());
        if (markerIndex <= prefix.length()) {
            return null;
        }
        return value.substring(prefix.length(), markerIndex);
    }

    private static String tryCompletion(String value) {
        String prefix = "Program ";
        if (!value.startsWith(prefix)) {
            return null;
        }
        int successIndex = value.indexOf(" success", prefix.length());
        int failedIndex = value.indexOf(" failed:", prefix.length());
        int markerIndex = successIndex >= 0 ? successIndex : failedIndex;
        if (markerIndex <= prefix.length()) {
            return null;
        }
        return value.substring(prefix.length(), markerIndex);
    }

    private static boolean startsWith(byte[] buffer, byte[] prefix) {
        if (buffer.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (buffer[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] eventDiscriminator(String name) {
        return Arrays.copyOf(sha256(("event:" + name).getBytes(StandardCharsets.UTF_8)), 8);
    }

    public static byte[] accountDiscriminator(String name) {
        return Arrays.copyOf(sha256(("account:" + name).getBytes(StandardCharsets.UTF_8)), 8);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

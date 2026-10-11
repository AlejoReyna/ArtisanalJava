package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Port of {@code Blockchain/WalletAddressRules.cs}.
 * Checksum via EIP-55 (Keccak-256 of lowercase hex), replacing Nethereum's
 * {@code AddressUtil}.
 */
public final class WalletAddressRules {
    private static final String ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

    private WalletAddressRules() {
    }

    public static boolean isValidEthereumAddress(String address) {
        return address != null && address.length() == 42
                && address.regionMatches(true, 0, "0x", 0, 2)
                && isHexDigits(address.substring(2));
    }

    public static Optional<String> tryNormalizeWallet(String address) {
        if (!isValidEthereumAddress(address)) {
            return Optional.empty();
        }
        return Optional.of(toChecksumAddress(address));
    }

    public static boolean isConfiguredAddress(String address) {
        return isValidEthereumAddress(address) && !address.equalsIgnoreCase(ZERO_ADDRESS);
    }

    public static Optional<String> tryNormalizeTransactionHash(String value) {
        if (value == null || value.length() != 66
                || !value.regionMatches(true, 0, "0x", 0, 2)
                || !isHexDigits(value.substring(2))) {
            return Optional.empty();
        }
        return Optional.of(value.toLowerCase(java.util.Locale.ROOT));
    }

    /** EIP-55 checksum encoding. */
    public static String toChecksumAddress(String address) {
        String lower = address.substring(2).toLowerCase(java.util.Locale.ROOT);
        byte[] hash = StakingCalldataDecoder.keccak256(lower.getBytes(StandardCharsets.UTF_8));
        String hex = HexFormat.of().formatHex(hash);
        StringBuilder out = new StringBuilder("0x");
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c >= 'a' && c <= 'f' && Character.digit(hex.charAt(i), 16) >= 8) {
                out.append(Character.toUpperCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    static boolean isHexDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.digit(value.charAt(i), 16) < 0) {
                return false;
            }
        }
        return !value.isEmpty();
    }
}

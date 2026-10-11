package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/**
 * Port of {@code Blockchain/SolanaBase58.cs} — verbatim algorithm.
 * Pure value conversion shared by Web, Worker and gateways.
 */
public final class SolanaBase58 {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private SolanaBase58() {
    }

    public static boolean isPublicKey(String value) {
        byte[] bytes = tryDecode(value);
        return bytes != null && bytes.length == 32;
    }

    public static byte[] tryDecode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        byte[] digits = new byte[(value.length() * 733 / 1000) + 1];
        int length = 1;
        for (int ci = 0; ci < value.length(); ci++) {
            int carry = ALPHABET.indexOf(value.charAt(ci));
            if (carry < 0) {
                return null;
            }
            for (int i = 0; i < length; i++) {
                carry += 58 * (digits[i] & 0xFF);
                digits[i] = (byte) (carry % 256);
                carry /= 256;
            }
            while (carry > 0) {
                digits[length++] = (byte) (carry % 256);
                carry /= 256;
            }
        }
        while (length > 0 && digits[length - 1] == 0) {
            length--;
        }
        int zeros = 0;
        while (zeros < value.length() && value.charAt(zeros) == '1') {
            zeros++;
        }
        byte[] bytes = new byte[zeros + length];
        for (int i = 0; i < length; i++) {
            bytes[bytes.length - 1 - i] = digits[i];
        }
        return bytes;
    }

    public static String encode(byte[] bytes) {
        byte[] digits = new byte[bytes.length * 138 / 100 + 1];
        int length = 1;
        for (byte value : bytes) {
            int carry = value & 0xFF;
            for (int i = 0; i < length; i++) {
                carry += 256 * (digits[i] & 0xFF);
                digits[i] = (byte) (carry % 58);
                carry /= 58;
            }
            while (carry > 0) {
                digits[length++] = (byte) (carry % 58);
                carry /= 58;
            }
        }
        StringBuilder result = new StringBuilder(bytes.length * 2);
        int i = 0;
        while (i < bytes.length && bytes[i] == 0) {
            result.append('1');
            i++;
        }
        for (int j = length - 1; j >= 0; j--) {
            result.append(ALPHABET.charAt(digits[j] & 0xFF));
        }
        return result.toString();
    }
}

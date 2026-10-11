package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Port of {@code Blockchain/SolanaAccountCodec.cs}.
 * C# {@code BinaryPrimitives} → {@link ByteBuffer#order(ByteOrder) LITTLE_ENDIAN};
 * C# unsigned {@code BigInteger(span, isUnsigned, isBigEndian:false)} → reversed copy.
 */
public final class SolanaAccountCodec {
    public static final int VAULT_ACCOUNT_SIZE = 189;

    private SolanaAccountCodec() {
    }

    public static SolanaVaultAccountState decodeVault(byte[] bytes) {
        if (bytes == null || bytes.length < VAULT_ACCOUNT_SIZE) {
            throw new IllegalArgumentException("The Solana vault account is truncated.");
        }
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int cafeDecimals = bytes[9] & 0xFF;
        int coffeeDecimals = bytes[10] & 0xFF;
        long totalShares = buf.getLong(141);
        BigInteger rewardPerShare = littleEndianUnsigned(Arrays.copyOfRange(bytes, 149, 165));
        long rewardRate = buf.getLong(165);
        long periodFinish = buf.getLong(173);
        long lastRewardSlot = buf.getLong(181);
        return new SolanaVaultAccountState(cafeDecimals, coffeeDecimals, totalShares,
                rewardPerShare, rewardRate, periodFinish, lastRewardSlot);
    }

    public static SolanaMintAccountState decodeMint(byte[] bytes) {
        if (bytes == null || bytes.length < 82) {
            throw new IllegalArgumentException("The Solana mint account is truncated.");
        }
        return new SolanaMintAccountState(bytes[44] & 0xFF,
                readOptionalPublicKey(bytes, 0, 4), readOptionalPublicKey(bytes, 46, 50));
    }

    public static int decodeMintDecimals(byte[] bytes) {
        return decodeMint(bytes).decimals();
    }

    private static byte[] readOptionalPublicKey(byte[] bytes, int optionOffset, int keyOffset) {
        int option = ByteBuffer.wrap(bytes, optionOffset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return option == 1 ? Arrays.copyOfRange(bytes, keyOffset, keyOffset + 32) : null;
    }

    private static BigInteger littleEndianUnsigned(byte[] littleEndian) {
        byte[] bigEndian = new byte[littleEndian.length];
        for (int i = 0; i < littleEndian.length; i++) {
            bigEndian[i] = littleEndian[littleEndian.length - 1 - i];
        }
        return new BigInteger(1, bigEndian);
    }
}

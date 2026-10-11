package dev.alexisreyna.artisanalbrew.domain.avatars;

import java.nio.charset.StandardCharsets;

/**
 * Port of {@code ThisCafeteria.Domain/Avatars/AvatarSeed.cs}.
 *
 * <p>Derives a wallet's starter robot from its address (robot identicon). Uses FNV-1a
 * explicitly because {@code String.hashCode()} — like .NET's {@code GetHashCode()} —
 * is not stable across processes/releases.</p>
 *
 * <p><strong>C# → Java pitfall:</strong> C# uses {@code ulong} arithmetic (wraps mod 2^64,
 * unsigned remainder). Java {@code long} wraps identically at the bit level, but the
 * {@code %} operator is <em>signed</em>. This port therefore uses
 * {@link Long#remainderUnsigned(long, long)} for slot selection so distant wallets land
 * on the same sprite as .NET.</p>
 */
public final class AvatarSeed {
    private static final long FNV_OFFSET_BASIS = -3750763034362895579L; // 14695981039346656037 as signed long
    private static final long FNV_PRIME = 1099511628211L;

    private AvatarSeed() {
    }

    /** The starter look for a wallet, or the catalog defaults when none is linked yet. */
    public static RobotAvatar fromWallet(String walletAddress) {
        String normalized = normalizeAddress(walletAddress);
        if (normalized.isEmpty()) {
            return AvatarCatalog.createDefault();
        }
        RobotAvatar avatar = new RobotAvatar();
        for (AvatarSlot slot : AvatarCatalog.getSlots()) {
            // Mix the slot key into the input so slots stay independent: adding a
            // seventh slot later must not reshuffle the six wallets already show.
            long hash = hash(normalized + ":" + slot.getKey());
            int index = (int) Long.remainderUnsigned(hash, slot.getItems().size());
            avatar.set(slot.getKey(), slot.getItems().get(index).id());
        }
        return avatar;
    }

    /** Whether {@code avatar} is exactly what this wallet seeds to (never edited). */
    public static boolean isUnchangedSeed(String walletAddress, RobotAvatar avatar) {
        return avatar != null && fromWallet(walletAddress).hasSameLook(AvatarCatalog.normalize(avatar));
    }

    /**
     * Case-folds EVM addresses only. Base58 (Solana) addresses are case-significant
     * and left alone — lower-casing them would collapse distinct wallets.
     */
    static String normalizeAddress(String walletAddress) {
        String trimmed = walletAddress == null ? "" : walletAddress.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        boolean isHex = trimmed.length() > 2
                && trimmed.regionMatches(true, 0, "0x", 0, 2)
                && isHexDigits(trimmed.substring(2));
        return isHex ? trimmed.toLowerCase(java.util.Locale.ROOT) : trimmed;
    }

    static long hash(String value) {
        long hash = FNV_OFFSET_BASIS;
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            hash ^= (b & 0xFFL);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    private static boolean isHexDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean hex = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F');
            if (!hex) {
                return false;
            }
        }
        return true;
    }
}

package dev.alexisreyna.artisanalbrew.application.config;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaBase58;

/** Port of {@code Configuration/SolanaAddressRules.cs}. */
public final class SolanaAddressRules {
    public static final String TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
    public static final String TOKEN_2022_PROGRAM = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb";

    private SolanaAddressRules() {
    }

    public static boolean isPublicKey(String value) {
        return SolanaBase58.isPublicKey(value);
    }

    public static byte[] tryDecode(String value) {
        return SolanaBase58.tryDecode(value);
    }
}

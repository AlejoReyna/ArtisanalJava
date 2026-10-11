package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code SolanaMintAccountState} in {@code Blockchain/SolanaAccountCodec.cs}. */
public record SolanaMintAccountState(int decimals, byte[] mintAuthority, byte[] freezeAuthority) {
}

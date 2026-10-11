package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code SolanaAnchorEvent} in {@code Blockchain/SolanaAnchorEventCodec.cs}. */
public record SolanaAnchorEvent(String name, int logIndex, byte[] payload) {
}

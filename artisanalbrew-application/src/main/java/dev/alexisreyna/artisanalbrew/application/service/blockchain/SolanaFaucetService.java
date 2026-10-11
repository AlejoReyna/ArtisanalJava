package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code ISolanaFaucetService} in {@code Blockchain/ISolanaFaucetService.cs}. */
public interface SolanaFaucetService {
    CafeFaucetStatus getStatus(String chainKey, String wallet);

    SolanaFaucetClaimResult claim(String chainKey, String wallet);
}

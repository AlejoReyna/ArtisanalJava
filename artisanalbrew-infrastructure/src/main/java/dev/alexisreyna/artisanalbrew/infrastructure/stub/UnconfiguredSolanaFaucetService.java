package dev.alexisreyna.artisanalbrew.infrastructure.stub;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.CafeFaucetStatus;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetClaimResult;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaFaucetService;

import java.math.BigDecimal;

/** Fail-closed Solana faucet: unconfigured, never mints. */
public class UnconfiguredSolanaFaucetService implements SolanaFaucetService {

    @Override
    public CafeFaucetStatus getStatus(String chainKey, String wallet) {
        return new CafeFaucetStatus();
    }

    @Override
    public SolanaFaucetClaimResult claim(String chainKey, String wallet) {
        return new SolanaFaucetClaimResult(false, null, null, BigDecimal.ZERO,
                "Solana faucet is not configured on the server.", null);
    }
}

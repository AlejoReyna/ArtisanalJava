package dev.alexisreyna.artisanalbrew.application.service.wallet;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;

/** Port of {@code ISolanaWalletChallengeService} in {@code Wallet/ISolanaWalletChallengeService.cs}. */
public interface SolanaWalletChallengeService {
    SolanaWalletChallenge create(String publicKey, ChainDefinition chain, String origin);

    SolanaChallengeVerificationError verify(String publicKey, String message, String nonce,
                                            String chainKey, String origin, String signature);
}

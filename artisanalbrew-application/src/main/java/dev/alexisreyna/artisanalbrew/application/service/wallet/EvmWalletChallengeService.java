package dev.alexisreyna.artisanalbrew.application.service.wallet;

/** Port of {@code IWalletChallengeService} in {@code Web/Services/Wallet/WalletChallengeService.cs}. */
public interface EvmWalletChallengeService {
    EvmWalletChallenge createChallenge(String address, int chainId, String networkName, String origin);

    EvmWalletChallenge createChallenge(String address, int chainId, String networkName, String origin,
                                       String chainKey, String family);

    record Verification(EvmChallengeVerificationError error, String recoveredAddress) {
    }

    Verification verify(String address, String message, String nonce, int chainId, String signature);

    Verification verify(String address, String message, String nonce, int chainId, String signature,
                        String chainKey, String family);
}

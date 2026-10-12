package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallenge;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallengeService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Port of {@code WalletChallengeService} ({@code Web/Services/Wallet}).
 * .NET keeps EVM challenges in {@code IMemoryCache}; here a bounded
 * {@link ConcurrentHashMap} with lazy expiry. Single-use: any verify attempt
 * removes the challenge first, so a replay always reads {@code EXPIRED}.
 */
public final class DefaultEvmWalletChallengeService implements EvmWalletChallengeService {
    private static final Duration LIFETIME = Duration.ofMinutes(5);

    private record CachedChallenge(String address, String message, int chainId, String chainKey, String family,
                                   OffsetDateTime expiresAt) {
    }

    private final Map<String, CachedChallenge> cache = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public EvmWalletChallenge createChallenge(String address, int chainId, String networkName, String origin) {
        return createChallenge(address, chainId, networkName, origin, "ethereum-sepolia", "Evm");
    }

    @Override
    public EvmWalletChallenge createChallenge(String address, int chainId, String networkName, String origin,
                                              String chainKey, String family) {
        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);
        String nonce = HexFormat.of().formatHex(nonceBytes).toLowerCase(Locale.ROOT);
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plus(LIFETIME);
        String message = String.join("\n",
                "ThisCafeteria wallet login",
                "",
                "Address: " + address,
                "Chain ID: " + chainId,
                "Chain Key: " + chainKey,
                "Family: " + family,
                "Network: " + networkName,
                "URI: " + origin,
                "Version: 1",
                "Nonce: " + nonce,
                "Issued At: " + issuedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "Expiration Time: " + expiresAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        cache.put(cacheKey(nonce), new CachedChallenge(address, message, chainId, chainKey, family, expiresAt));
        return new EvmWalletChallenge(message, nonce, issuedAt, expiresAt);
    }

    @Override
    public Verification verify(String address, String message, String nonce, int chainId, String signature) {
        return verify(address, message, nonce, chainId, signature, "ethereum-sepolia", "Evm");
    }

    @Override
    public Verification verify(String address, String message, String nonce, int chainId, String signature,
                               String chainKey, String family) {
        CachedChallenge challenge = cache.remove(cacheKey(nonce));
        if (challenge == null || OffsetDateTime.now(ZoneOffset.UTC).isAfter(challenge.expiresAt())) {
            return new Verification(EvmChallengeVerificationError.EXPIRED, "");
        }
        if (!challenge.address().equalsIgnoreCase(address)
                || !challenge.message().equals(message)
                || challenge.chainId() != chainId
                || !challenge.chainKey().equals(chainKey)
                || !challenge.family().equals(family)) {
            return new Verification(EvmChallengeVerificationError.MISMATCH, "");
        }
        String recovered = EvmSignatureRecovery.recoverAddress(message, signature).orElse("");
        if (recovered.isEmpty() || !recovered.equalsIgnoreCase(address)) {
            return new Verification(EvmChallengeVerificationError.INVALID_SIGNATURE, recovered);
        }
        return new Verification(EvmChallengeVerificationError.NONE, recovered);
    }

    private static String cacheKey(String nonce) {
        return "wallet-auth:" + nonce;
    }
}

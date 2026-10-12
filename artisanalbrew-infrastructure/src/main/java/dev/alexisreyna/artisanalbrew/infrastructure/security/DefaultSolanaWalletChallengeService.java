package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.repository.WalletAuthChallengeRepository;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaBase58;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallenge;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallengeService;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletAuthChallenge;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Port of {@code SolanaWalletChallengeService} ({@code Infrastructure/Services/Wallet}).
 * Message shape, SHA-256 hex hashing, 5-minute lifetime and the
 * check-everything-then-consume-atomically order are preserved exactly.
 */
public final class DefaultSolanaWalletChallengeService implements SolanaWalletChallengeService {
    private static final Duration LIFETIME = Duration.ofMinutes(5);
    private static final int PUBLIC_KEY_LENGTH = 32;
    private static final int SIGNATURE_LENGTH = 64;

    private final WalletAuthChallengeRepository challenges;
    private final SecureRandom random = new SecureRandom();

    public DefaultSolanaWalletChallengeService(WalletAuthChallengeRepository challenges) {
        this.challenges = challenges;
    }

    @Override
    public SolanaWalletChallenge create(String publicKey, ChainDefinition chain, String origin) {
        byte[] keyBytes = SolanaBase58.tryDecode(publicKey);
        if (keyBytes == null || keyBytes.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid Solana public key.");
        }
        byte[] nonceBytes = new byte[24];
        random.nextBytes(nonceBytes);
        String nonce = HexFormat.of().formatHex(nonceBytes).toLowerCase(Locale.ROOT);
        OffsetDateTime issued = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expires = issued.plus(LIFETIME);
        String message = String.join("\n",
                "ThisCafeteria Solana wallet login",
                "",
                "Application: " + origin,
                "URI: " + origin,
                "Solana Public Key: " + publicKey,
                "Chain Key: " + chain.getKey(),
                "Cluster: " + chain.getSolanaCluster(),
                "Purpose: Authenticate wallet ownership for ThisCafeteria",
                "Version: 1",
                "Nonce: " + nonce,
                "Issued At: " + issued.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "Expiration Time: " + expires.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        challenges.prune(issued.toInstant());

        WalletAuthChallenge challenge = new WalletAuthChallenge();
        challenge.setNonceHash(hash(nonce));
        challenge.setPublicKey(publicKey);
        challenge.setChainKey(chain.getKey());
        challenge.setOrigin(origin);
        challenge.setMessageHash(hash(message));
        challenge.setIssuedAtUtc(issued.toInstant());
        challenge.setExpiresAtUtc(expires.toInstant());
        challenges.add(challenge);
        return new SolanaWalletChallenge(message, nonce, issued, expires);
    }

    @Override
    public SolanaChallengeVerificationError verify(String publicKey, String message, String nonce,
                                                  String chainKey, String origin, String signature) {
        Instant now = Instant.now();
        String nonceHash = hash(nonce);
        WalletAuthChallenge challenge = challenges.findByNonceHash(nonceHash).orElse(null);
        if (challenge == null || challenge.getConsumedAtUtc() != null
                || challenge.getExpiresAtUtc() == null || !challenge.getExpiresAtUtc().isAfter(now)) {
            return SolanaChallengeVerificationError.EXPIRED;
        }
        // A signature captured for one origin or chain must not replay elsewhere.
        if (!publicKey.equals(challenge.getPublicKey())
                || !hash(message).equals(challenge.getMessageHash())
                || !chainKey.equals(challenge.getChainKey())
                || !origin.equals(challenge.getOrigin())) {
            return SolanaChallengeVerificationError.MISMATCH;
        }
        byte[] signatureBytes = SolanaBase58.tryDecode(signature);
        if (signatureBytes == null || signatureBytes.length != SIGNATURE_LENGTH) {
            return SolanaChallengeVerificationError.INVALID_SIGNATURE;
        }
        byte[] publicKeyBytes = SolanaBase58.tryDecode(publicKey);
        if (publicKeyBytes == null || publicKeyBytes.length != PUBLIC_KEY_LENGTH) {
            return SolanaChallengeVerificationError.MISMATCH;
        }
        Ed25519Signer verifier = new Ed25519Signer();
        verifier.init(false, new Ed25519PublicKeyParameters(publicKeyBytes, 0));
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        verifier.update(messageBytes, 0, messageBytes.length);
        if (!verifier.verifySignature(signatureBytes)) {
            return SolanaChallengeVerificationError.INVALID_SIGNATURE;
        }
        // Consumed only after the signature checks out, atomically.
        if (!challenges.tryConsume(nonceHash, now)) {
            return SolanaChallengeVerificationError.EXPIRED;
        }
        return SolanaChallengeVerificationError.NONE;
    }

    static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

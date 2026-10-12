package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.config.ChainDefinition;
import dev.alexisreyna.artisanalbrew.application.config.ChainFamily;
import dev.alexisreyna.artisanalbrew.application.repository.WalletAuthChallengeRepository;
import dev.alexisreyna.artisanalbrew.application.service.blockchain.SolanaBase58;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.SolanaWalletChallenge;
import dev.alexisreyna.artisanalbrew.domain.entities.WalletAuthChallenge;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Full Solana challenge flow against an in-memory challenge store and a fresh
 * Ed25519 keypair: message binding, origin/chain replay rejection, tampered
 * signatures, and single-use consumption.
 */
class SolanaWalletChallengeServiceTest {

    static final class FakeChallenges implements WalletAuthChallengeRepository {
        final Map<String, WalletAuthChallenge> store = new HashMap<>();

        @Override
        public void prune(Instant now, int maxRows) {
            List<String> drop = new ArrayList<>();
            for (var e : store.entrySet()) {
                WalletAuthChallenge c = e.getValue();
                if (c.getExpiresAtUtc().isBefore(now.minusSeconds(60))
                        || (c.getConsumedAtUtc() != null && c.getConsumedAtUtc().isBefore(now.minusSeconds(600)))) {
                    drop.add(e.getKey());
                }
            }
            drop.stream().limit(maxRows).forEach(store::remove);
        }

        @Override
        public void add(WalletAuthChallenge challenge) {
            store.put(challenge.getNonceHash(), challenge);
        }

        @Override
        public Optional<WalletAuthChallenge> findByNonceHash(String nonceHash) {
            return Optional.ofNullable(store.get(nonceHash));
        }

        @Override
        public boolean tryConsume(String nonceHash, Instant now) {
            WalletAuthChallenge c = store.get(nonceHash);
            if (c == null || c.getConsumedAtUtc() != null || !c.getExpiresAtUtc().isAfter(now)) {
                return false;
            }
            c.setConsumedAtUtc(now);
            c.setVerificationAttempts(c.getVerificationAttempts() + 1);
            return true;
        }
    }

    record Keypair(String publicKey, Ed25519PrivateKeyParameters privateKey) {
    }

    static Keypair freshKeypair() {
        Ed25519KeyPairGenerator generator = new Ed25519KeyPairGenerator();
        generator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair pair = generator.generateKeyPair();
        Ed25519PublicKeyParameters pub = (Ed25519PublicKeyParameters) pair.getPublic();
        return new Keypair(SolanaBase58.encode(pub.getEncoded()), (Ed25519PrivateKeyParameters) pair.getPrivate());
    }

    static String sign(Keypair keys, String message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, keys.privateKey());
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        signer.update(bytes, 0, bytes.length);
        return SolanaBase58.encode(signer.generateSignature());
    }

    static ChainDefinition devnet() {
        ChainDefinition chain = new ChainDefinition();
        chain.setKey("solana-devnet");
        chain.setFamily(ChainFamily.SOLANA);
        chain.setSolanaCluster("devnet");
        return chain;
    }

    @Test
    void createRejectsNonKeysAndBindsMessage() {
        var service = new DefaultSolanaWalletChallengeService(new FakeChallenges());
        assertThatThrownBy(() -> service.create("nope", devnet(), "https://cafe.example"))
                .isInstanceOf(IllegalArgumentException.class);
        Keypair keys = freshKeypair();
        SolanaWalletChallenge challenge = service.create(keys.publicKey(), devnet(), "https://cafe.example");
        assertThat(challenge.message()).contains("Solana Public Key: " + keys.publicKey());
        assertThat(challenge.message()).contains("Chain Key: solana-devnet");
    }

    @Test
    void fullVerifyFlowWithReplayAndTamperRejection() {
        FakeChallenges store = new FakeChallenges();
        var service = new DefaultSolanaWalletChallengeService(store);
        Keypair keys = freshKeypair();
        SolanaWalletChallenge challenge =
                service.create(keys.publicKey(), devnet(), "https://cafe.example");

        var ok = service.verify(keys.publicKey(), challenge.message(), challenge.nonce(),
                "solana-devnet", "https://cafe.example", sign(keys, challenge.message()));
        assertThat(ok).isEqualTo(SolanaChallengeVerificationError.NONE);

        // Same signature again: consumed → EXPIRED.
        var replay = service.verify(keys.publicKey(), challenge.message(), challenge.nonce(),
                "solana-devnet", "https://cafe.example", sign(keys, challenge.message()));
        assertThat(replay).isEqualTo(SolanaChallengeVerificationError.EXPIRED);

        // Cross-origin and cross-chain replays must not verify.
        SolanaWalletChallenge second =
                service.create(keys.publicKey(), devnet(), "https://cafe.example");
        String sig = sign(keys, second.message());
        assertThat(service.verify(keys.publicKey(), second.message(), second.nonce(),
                "solana-devnet", "https://evil.example", sig))
                .isEqualTo(SolanaChallengeVerificationError.MISMATCH);
        assertThat(service.verify(keys.publicKey(), second.message(), second.nonce(),
                "solana-testnet", "https://cafe.example", sig))
                .isEqualTo(SolanaChallengeVerificationError.MISMATCH);

        // Tampered signature bytes.
        SolanaWalletChallenge third =
                service.create(keys.publicKey(), devnet(), "https://cafe.example");
        byte[] bad = SolanaBase58.tryDecode(sign(keys, third.message()));
        bad[0] ^= 0x01;
        assertThat(service.verify(keys.publicKey(), third.message(), third.nonce(),
                "solana-devnet", "https://cafe.example", SolanaBase58.encode(bad)))
                .isEqualTo(SolanaChallengeVerificationError.INVALID_SIGNATURE);
    }
}

package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmChallengeVerificationError;
import dev.alexisreyna.artisanalbrew.application.service.wallet.EvmWalletChallenge;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hardhat account #0 is the anchor: private key
 * {@code ac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7b4d3ff3}
 * must derive {@code 0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266}, and a
 * deterministic (RFC 6979) signature over the service challenge must recover
 * to it through the full personal_sign path.
 */
class EvmWalletAuthTest {

    private static final BigInteger HARDHAT_KEY =
            new BigInteger("ac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80", 16);
    private static final String HARDHAT_ADDRESS = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";

    /**
     * Independently generated with ethers v6
     * ({@code Wallet.fromPhrase(hardhat mnemonic).signMessage("VECTOR-MESSAGE-1")}).
     */
    private static final String ETHERS_MESSAGE = "VECTOR-MESSAGE-1";
    private static final String ETHERS_SIGNATURE =
            "0xd9a8b858e18911f2d8df4899fb7da538896fe4b5f7ca371ad13c12919a2ac36c20cb586b2bda82ae7e42145d91723560a3e8ff53e1a2a00e83a8ac9a33504a341b";

    @Test
    void recoversEthersProducedSignature() {
        assertThat(EvmSignatureRecovery.recoverAddress(ETHERS_MESSAGE, ETHERS_SIGNATURE))
                .hasValue(HARDHAT_ADDRESS);
    }

    static String signDeterministic(String message, BigInteger privateKey) {
        X9ECParameters curve = CustomNamedCurves.getByName("secp256k1");
        ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, new ECPrivateKeyParameters(privateKey, domain));
        byte[] hash = EvmSignatureRecovery.personalMessageHash(message);
        BigInteger[] rs = signer.generateSignature(hash);
        BigInteger s = EvmSignatureRecovery.isCanonicalS(rs[1]) ? rs[1] : domain.getN().subtract(rs[1]);
        int recId = -1;
        for (int candidate = 0; candidate < 4; candidate++) {
            BigInteger recovered;
            try {
                recovered = EvmSignatureRecovery.recoverPublicKey(candidate, rs[0], s, hash);
            } catch (Exception e) {
                continue;
            }
            if (recovered == null) {
                continue;
            }
            byte[] pubBytes = EvmSignatureRecovery.toBytesPadded(recovered, 64);
            byte[] digest = dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder
                    .keccak256(pubBytes);
            String address = "0x" + HexFormat.of().formatHex(digest, 12, 32);
            if (address.equalsIgnoreCase(HARDHAT_ADDRESS)) {
                recId = candidate;
                break;
            }
        }
        assertThat(recId).isGreaterThanOrEqualTo(0);
        byte[] sig = new byte[65];
        System.arraycopy(EvmSignatureRecovery.toBytesPadded(rs[0], 32), 0, sig, 0, 32);
        System.arraycopy(EvmSignatureRecovery.toBytesPadded(s, 32), 0, sig, 32, 32);
        sig[64] = (byte) (recId + 27);
        return "0x" + HexFormat.of().formatHex(sig);
    }

    @Test
    void hardhatKeyDerivesKnownAddress() {
        X9ECParameters curve = CustomNamedCurves.getByName("secp256k1");
        var point = curve.getG().multiply(HARDHAT_KEY).normalize();
        byte[] encoded = point.getEncoded(false);
        byte[] digest = dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder
                .keccak256(java.util.Arrays.copyOfRange(encoded, 1, encoded.length));
        String address = "0x" + HexFormat.of().formatHex(digest, 12, 32);
        assertThat(dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules
                .toChecksumAddress(address)).isEqualTo(HARDHAT_ADDRESS);
    }

    @Test
    void challengeRoundTripThroughPersonalSign() {
        var service = new DefaultEvmWalletChallengeService();
        EvmWalletChallenge challenge = service.createChallenge(
                HARDHAT_ADDRESS, 11155111, "Ethereum Sepolia", "https://cafe.example");
        assertThat(challenge.message()).startsWith("ThisCafeteria wallet login\n\nAddress: " + HARDHAT_ADDRESS);

        String signature = signDeterministic(challenge.message(), HARDHAT_KEY);
        var ok = service.verify(HARDHAT_ADDRESS, challenge.message(), challenge.nonce(), 11155111, signature);
        assertThat(ok.error()).isEqualTo(EvmChallengeVerificationError.NONE);
        assertThat(ok.recoveredAddress()).isEqualTo(HARDHAT_ADDRESS);

        // Single-use: the replay reads EXPIRED.
        var replay = service.verify(HARDHAT_ADDRESS, challenge.message(), challenge.nonce(), 11155111, signature);
        assertThat(replay.error()).isEqualTo(EvmChallengeVerificationError.EXPIRED);
    }

    @Test
    void wrongSignerAndWrongChainRejected() {
        var service = new DefaultEvmWalletChallengeService();
        EvmWalletChallenge challenge = service.createChallenge(
                HARDHAT_ADDRESS, 11155111, "Ethereum Sepolia", "https://cafe.example");
        String signature = signDeterministic(challenge.message(), HARDHAT_KEY);

        EvmWalletChallenge other = service.createChallenge(
                HARDHAT_ADDRESS, 11155111, "Ethereum Sepolia", "https://cafe.example");
        var wrongAddress = service.verify(
                "0x0000000000000000000000000000000000000001",
                other.message(), other.nonce(), 11155111, signature);
        assertThat(wrongAddress.error()).isIn(
                EvmChallengeVerificationError.MISMATCH, EvmChallengeVerificationError.INVALID_SIGNATURE);

        EvmWalletChallenge third = service.createChallenge(
                HARDHAT_ADDRESS, 11155111, "Ethereum Sepolia", "https://cafe.example");
        String thirdSig = signDeterministic(third.message(), HARDHAT_KEY);
        var wrongChain = service.verify(HARDHAT_ADDRESS, third.message(), third.nonce(), 97, thirdSig);
        assertThat(wrongChain.error()).isEqualTo(EvmChallengeVerificationError.MISMATCH);
    }
}

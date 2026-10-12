package dev.alexisreyna.artisanalbrew.infrastructure.security;

import dev.alexisreyna.artisanalbrew.application.service.blockchain.StakingCalldataDecoder;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;

/**
 * secp256k1 {@code personal_sign} recovery (mirrors Nethereum's
 * {@code EthereumMessageSigner.EncodeUTF8AndEcRecover}).
 */
public final class EvmSignatureRecovery {
    private static final X9ECParameters CURVE = CustomNamedCurves.getByName("secp256k1");
    private static final BigInteger N = CURVE.getN();
    private static final BigInteger HALF_N = N.shiftRight(1);

    private EvmSignatureRecovery() {
    }

    /** Keccak of {@code "\x19Ethereum Signed Message:\n" + utf8Length + message}. */
    public static byte[] personalMessageHash(String message) {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        byte[] prefix = ("\u0019Ethereum Signed Message:\n" + body.length).getBytes(StandardCharsets.UTF_8);
        byte[] input = Arrays.copyOf(prefix, prefix.length + body.length);
        System.arraycopy(body, 0, input, prefix.length, body.length);
        return StakingCalldataDecoder.keccak256(input);
    }

    /**
     * Recovers the checksummed signer address from a 65-byte (r || s || v)
     * hex signature. Accepts v in {27, 28} (wallets) or {0, 1} (raw).
     */
    public static Optional<String> recoverAddress(String message, String signatureHex) {
        byte[] sig;
        try {
            String hex = signatureHex.startsWith("0x") || signatureHex.startsWith("0X")
                    ? signatureHex.substring(2) : signatureHex;
            sig = HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (sig.length != 65) {
            return Optional.empty();
        }
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sig, 0, 32));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sig, 32, 64));
        int v = sig[64] & 0xFF;
        int recId;
        if (v == 27 || v == 28) {
            recId = v - 27;
        } else if (v == 0 || v == 1) {
            recId = v;
        } else {
            return Optional.empty();
        }
        if (r.signum() <= 0 || r.compareTo(N) >= 0 || s.signum() <= 0 || s.compareTo(N) >= 0) {
            return Optional.empty();
        }
        byte[] hash = personalMessageHash(message);
        BigInteger publicKey = recoverPublicKey(recId, r, s, hash);
        if (publicKey == null) {
            return Optional.empty();
        }
        byte[] pubBytes = toBytesPadded(publicKey, 64);
        byte[] digest = StakingCalldataDecoder.keccak256(pubBytes);
        String address = "0x" + HexFormat.of().formatHex(digest, 12, 32);
        return Optional.of(dev.alexisreyna.artisanalbrew.application.service.blockchain.WalletAddressRules
                .toChecksumAddress(address));
    }

    /** Canonicalizes s to its low half (EIP-2 style); returns null when unrecoverable. */
    static BigInteger recoverPublicKey(int recId, BigInteger r, BigInteger s, byte[] hash) {
        BigInteger n = N;
        BigInteger i = BigInteger.valueOf(recId / 2);
        BigInteger x = r.add(i.multiply(n));
        SecP256K1Curve curve = (SecP256K1Curve) CURVE.getCurve();
        if (x.compareTo(curve.getQ()) >= 0) {
            return null;
        }
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        BigInteger e = new BigInteger(1, hash);
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = r.modInverse(n);
        BigInteger srInv = rInv.multiply(s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = CURVE.getG().multiply(eInvrInv).add(R.multiply(srInv));
        byte[] encoded = q.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length));
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        byte[] xBytes = toBytesPadded(xBN, 32);
        byte[] encoded = new byte[33];
        encoded[0] = (byte) (yBit ? 0x03 : 0x02);
        System.arraycopy(xBytes, 0, encoded, 1, 32);
        return CURVE.getCurve().decodePoint(encoded);
    }

    static byte[] toBytesPadded(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();
        if (bytes.length == length) {
            return bytes;
        }
        byte[] padded = new byte[length];
        if (bytes.length > length) {
            System.arraycopy(bytes, bytes.length - length, padded, 0, length);
        } else {
            System.arraycopy(bytes, 0, padded, length - bytes.length, bytes.length);
        }
        return padded;
    }

    static boolean isCanonicalS(BigInteger s) {
        return s.compareTo(HALF_N) <= 0;
    }
}

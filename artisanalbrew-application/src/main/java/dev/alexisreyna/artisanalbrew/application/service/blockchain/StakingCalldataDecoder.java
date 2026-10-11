package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import org.bouncycastle.crypto.digests.KeccakDigest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Port of {@code Blockchain/StakingCalldataDecoder.cs}.
 * Nethereum's {@code Sha3Keccack} is Ethereum Keccak-256 (not NIST SHA3);
 * here via Bouncy Castle {@link KeccakDigest}.
 */
public final class StakingCalldataDecoder {
    public static final List<String> CLAIM_FUNCTION_NAMES = List.of("getReward", "claimReward");
    public static final List<String> CLAIM_FUNCTION_SIGNATURES = List.of("getReward()", "claimReward()");

    private static final String ERC20_TRANSFER_SELECTOR = "0xa9059cbb";

    private StakingCalldataDecoder() {
    }

    public static String functionSelector(String signature) {
        byte[] hash = keccak256(signature.getBytes(StandardCharsets.UTF_8));
        return "0x" + HexFormat.of().formatHex(hash, 0, 4);
    }

    public record Erc20Transfer(String recipient, BigInteger amount) {
    }

    public static Optional<Erc20Transfer> tryDecodeErc20Transfer(String input) {
        if (input == null || !input.regionMatches(true, 0, ERC20_TRANSFER_SELECTOR, 0, ERC20_TRANSFER_SELECTOR.length())
                || input.length() < 138) {
            return Optional.empty();
        }
        String recipient = "0x" + input.substring(10 + 24, 10 + 24 + 40);
        BigInteger amount = new BigInteger(input.substring(10 + 64, 10 + 64 + 64), 16);
        if (!WalletAddressRules.isValidEthereumAddress(recipient)) {
            return Optional.empty();
        }
        return Optional.of(new Erc20Transfer(recipient, amount));
    }

    public static Optional<BigInteger> tryDecodeStakingAmount(String input, StakingTransactionType transactionType) {
        if (input == null || input.length() < 74) {
            return Optional.empty();
        }
        String stakeSelector = functionSelector("stake(uint256)");
        String unstakeSelector = functionSelector("unstake(uint256)");
        String withdrawSelector = functionSelector("withdraw(uint256)");
        boolean matches = switch (transactionType) {
            case STAKE -> input.regionMatches(true, 0, stakeSelector, 0, stakeSelector.length());
            case UNSTAKE -> input.regionMatches(true, 0, unstakeSelector, 0, unstakeSelector.length())
                    || input.regionMatches(true, 0, withdrawSelector, 0, withdrawSelector.length());
            default -> false;
        };
        if (!matches) {
            return Optional.empty();
        }
        return Optional.of(new BigInteger(input.substring(10, 10 + 64), 16));
    }

    public static boolean tryDecodeClaimSelector(String input) {
        if (input == null || input.length() < 10) {
            return false;
        }
        return CLAIM_FUNCTION_SIGNATURES.stream()
                .map(StakingCalldataDecoder::functionSelector)
                .anyMatch(sel -> input.regionMatches(true, 0, sel, 0, sel.length()));
    }

    static byte[] keccak256(byte[] input) {
        KeccakDigest digest = new KeccakDigest(256);
        digest.update(input, 0, input.length);
        byte[] out = new byte[32];
        digest.doFinal(out, 0);
        return out;
    }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class BlockchainHelpersTest {

    @Test
    void confirmationsClampAtZero() {
        assertThat(ConfirmationCalculator.calculate(BigInteger.valueOf(10), BigInteger.valueOf(9))).isEqualTo(2);
        assertThat(ConfirmationCalculator.calculate(BigInteger.valueOf(5), null)).isEqualTo(0);
        assertThat(ConfirmationCalculator.calculate(BigInteger.valueOf(5), BigInteger.valueOf(9))).isEqualTo(0);
    }

    @Test
    void rawAmountTruncatesTowardZero() {
        assertThat(StakingAmountRules.toRawAmount(new BigDecimal("1.5"))).isEqualTo("1500000000000000000");
        assertThat(StakingAmountRules.toRawAmount(new BigDecimal("0.0000000000000000009"))).isEqualTo("0");
        assertThat(StakingAmountRules.isValidStakeAmount(new BigDecimal("0.01"))).isTrue();
        assertThat(StakingAmountRules.isValidUnstakeAmount(new BigDecimal("5"), new BigDecimal("4"))).isFalse();
    }

    @Test
    void keccakSelectorMatchesKnownErc20Transfer() {
        assertThat(StakingCalldataDecoder.functionSelector("transfer(address,uint256)")).isEqualTo("0xa9059cbb");
        String input = "0xa9059cbb"
                + "000000000000000000000000932d50e20f917b9bbbe2c40f30d43bcef0e93f90"
                + "0000000000000000000000000000000000000000000000000000000000000064";
        var transfer = StakingCalldataDecoder.tryDecodeErc20Transfer(input);
        assertThat(transfer).isPresent();
        assertThat(transfer.get().recipient()).isEqualTo("0x932d50e20f917b9bbbe2c40f30d43bcef0e93f90");
        assertThat(transfer.get().amount()).isEqualTo(BigInteger.valueOf(100));
    }

    @Test
    void claimSelectorDetectsBothRewardFunctions() {
        String getReward = StakingCalldataDecoder.functionSelector("getReward()") + "0000";
        String claimReward = StakingCalldataDecoder.functionSelector("claimReward()") + "0000";
        assertThat(StakingCalldataDecoder.tryDecodeClaimSelector(getReward)).isTrue();
        assertThat(StakingCalldataDecoder.tryDecodeClaimSelector(claimReward)).isTrue();
        assertThat(StakingCalldataDecoder.tryDecodeClaimSelector("0x12345678")).isFalse();
    }

    @Test
    void eip55ChecksumMatchesReferenceVector() {
        assertThat(WalletAddressRules.toChecksumAddress("0x5aaeb6053f3e94c9b9a09f33669435e7ef1beaed"))
                .isEqualTo("0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed");
        assertThat(WalletAddressRules.tryNormalizeWallet("0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed")).isPresent();
        assertThat(WalletAddressRules.tryNormalizeWallet("not-an-address")).isEmpty();
        assertThat(WalletAddressRules.tryNormalizeTransactionHash("0x" + "ab".repeat(32))).isPresent();
        assertThat(WalletAddressRules.isConfiguredAddress("0x0000000000000000000000000000000000000000")).isFalse();
    }

    @Test
    void base58RoundTripsAndValidatesProgramIds() {
        byte[] raw = HexFormat.of().parseHex("0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20");
        String encoded = SolanaBase58.encode(raw);
        assertThat(SolanaBase58.tryDecode(encoded)).isEqualTo(raw);
        assertThat(SolanaBase58.isPublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA")).isTrue();
        assertThat(SolanaBase58.isPublicKey("nope")).isFalse();
    }

    @Test
    void anchorCodecDiscriminatesTrustedProgramEvents() {
        String program = "CafeLiquidStaking111111111111111111111111111";
        byte[] disc = SolanaAnchorEventCodec.eventDiscriminator("DepositEvent");
        byte[] payload = new byte[]{(byte) 0x2a};
        byte[] buffer = new byte[8 + payload.length];
        System.arraycopy(disc, 0, buffer, 0, 8);
        System.arraycopy(payload, 0, buffer, 8, payload.length);
        String data = Base64.getEncoder().encodeToString(buffer);
        var events = SolanaAnchorEventCodec.decode(
                java.util.List.of("Program " + program + " invoke [1]", "Program data: " + data,
                        "Program " + program + " success"),
                program, SolanaAnchorEventCodec.DEPOSIT, SolanaAnchorEventCodec.REDEEM);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).name()).isEqualTo(SolanaAnchorEventCodec.DEPOSIT);
        assertThat(events.get(0).payload()).isEqualTo(payload);
    }
}

package dev.alexisreyna.artisanalbrew.application.service.blockchain;

import java.math.BigInteger;

/** Port of {@code Blockchain/ConfirmationCalculator.cs}. */
public final class ConfirmationCalculator {
    private ConfirmationCalculator() {
    }

    public static int calculate(BigInteger currentBlock, BigInteger receiptBlock) {
        if (receiptBlock == null) {
            return 0;
        }
        BigInteger confirmations = currentBlock.subtract(receiptBlock).add(BigInteger.ONE);
        if (confirmations.signum() < 0) {
            return 0;
        }
        return confirmations.intValue();
    }
}

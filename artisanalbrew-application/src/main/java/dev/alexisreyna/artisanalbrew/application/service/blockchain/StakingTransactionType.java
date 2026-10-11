package dev.alexisreyna.artisanalbrew.application.service.blockchain;

/** Port of {@code Blockchain/StakingTransactionType.cs}. Codes match C#. */
public enum StakingTransactionType {
    STAKE(0),
    UNSTAKE(1),
    CLAIM(2);

    private final int code;

    StakingTransactionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

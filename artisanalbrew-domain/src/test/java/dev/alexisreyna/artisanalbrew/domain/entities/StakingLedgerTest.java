package dev.alexisreyna.artisanalbrew.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StakingLedgerTest {

    @Test
    void createBuildsChainQualifiedIdentity() {
        StakingLedgerEntry entry = StakingLedgerEntry.create("ethereum-sepolia", "0xhash", 2);
        assertThat(entry.getOperationIdentity())
                .isEqualTo(new StakingOperationIdentity("ethereum-sepolia", "0xhash", 2));
    }

    @Test
    void rejectsBlankChainHashOrNegativeIndex() {
        assertThatThrownBy(() -> StakingOperationIdentity.create(" ", "0xhash", 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> StakingOperationIdentity.create("chain", "0xhash", -1))
                .isInstanceOf(IllegalStateException.class);
    }
}

package dev.alexisreyna.artisanalbrew.domain.avatars;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarTest {

    @Test
    void normalizeFallsBackToDefaultsWithoutMutatingInput() {
        RobotAvatar stored = new RobotAvatar();
        stored.set(AvatarCatalog.CHASSIS_SLOT, "nope-missing");
        stored.set(AvatarCatalog.WEAR_SLOT, "");

        RobotAvatar normalized = AvatarCatalog.normalize(stored);

        assertThat(normalized.get(AvatarCatalog.CHASSIS_SLOT)).isEqualTo("cream");
        assertThat(normalized.get(AvatarCatalog.WEAR_SLOT)).isEqualTo("none");
        // Input untouched — the .NET comment's SaveChanges hazard applies to JPA too.
        assertThat(stored.get(AvatarCatalog.CHASSIS_SLOT)).isEqualTo("nope-missing");
    }

    @Test
    void unknownSlotFailsLoudly() {
        RobotAvatar avatar = new RobotAvatar();
        assertThatThrownBy(() -> avatar.get("wings")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AvatarCatalog.getSlot("wings")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void seedIsStableAndCaseFoldsEvmOnly() {
        RobotAvatar a = AvatarSeed.fromWallet("0xABCDEF1234567890abcdef1234567890ABCDEF12");
        RobotAvatar b = AvatarSeed.fromWallet("0xabcdef1234567890abcdef1234567890abcdef12");
        assertThat(a.hasSameLook(b)).isTrue();
        assertThat(AvatarSeed.isUnchangedSeed("0xabcdef1234567890abcdef1234567890abcdef12", a)).isTrue();
    }

    @Test
    void emptyWalletSeedsCatalogDefaults() {
        assertThat(AvatarSeed.fromWallet(null).hasSameLook(AvatarCatalog.createDefault())).isTrue();
        assertThat(AvatarSeed.fromWallet("  ").hasSameLook(AvatarCatalog.createDefault())).isTrue();
    }
}

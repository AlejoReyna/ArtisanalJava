package dev.alexisreyna.artisanalbrew.application.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugGeneratorTest {

    @Test
    void createsLowerHyphenatedSlug() {
        assertThat(SlugGenerator.create("  Cold Brew Nº 2! ")).isEqualTo("cold-brew-n-2");
        assertThat(SlugGenerator.create("Espresso")).isEqualTo("espresso");
        assertThat(SlugGenerator.create("a--b")).isEqualTo("a-b");
    }
}

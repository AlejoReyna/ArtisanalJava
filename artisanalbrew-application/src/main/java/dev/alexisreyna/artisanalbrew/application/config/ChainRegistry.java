package dev.alexisreyna.artisanalbrew.application.config;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Port of {@code IChainRegistry} in {@code Configuration/IChainRegistry.cs}. */
public interface ChainRegistry {
    String defaultChainKey();

    List<ChainDefinition> all();

    Optional<ChainDefinition> tryGet(String key);

    default ChainDefinition getRequired(String key) {
        return tryGet(key)
                .filter(ChainDefinition::isEnabled)
                .orElseThrow(() -> new NoSuchElementException("Chain '" + key + "' is unknown or disabled."));
    }
}

package dev.alexisreyna.artisanalbrew.application.service;

import dev.alexisreyna.artisanalbrew.domain.entities.SmartAccountType;

/** Port of {@code SmartAccountInfo} in {@code Services/ISmartAccountService.cs}. */
public record SmartAccountInfo(
        String chainKey,
        String ownerAddress,
        SmartAccountType accountType,
        String accountAddress,
        String factoryAddress,
        boolean deployed,
        boolean implementationVerified) {
}

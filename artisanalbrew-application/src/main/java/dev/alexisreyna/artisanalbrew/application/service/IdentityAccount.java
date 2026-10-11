package dev.alexisreyna.artisanalbrew.application.service;

import java.util.UUID;

/** Port of {@code IdentityAccount} in {@code Services/IIdentityAccountService.cs}. */
public record IdentityAccount(String id, UUID userProfileId, String walletAddress) {
}

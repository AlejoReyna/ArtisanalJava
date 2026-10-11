package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code AgentPermissionGrantInfo} in {@code Services/ISmartAccountService.cs}. */
public record AgentPermissionGrantInfo(
        String targetAddress,
        String selector,
        String tokenAddress,
        String amountWei,
        String delegationHash,
        String description) {
}

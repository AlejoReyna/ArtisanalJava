package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code AgentPermissionGrantInput} in {@code Services/ISmartAccountService.cs}. */
public record AgentPermissionGrantInput(
        String targetAddress,
        String selector,
        String tokenAddress,
        String amountWei,
        String delegationHash,
        String description) {
}

package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Port of {@code ISmartAccountService} in {@code Services/ISmartAccountService.cs}. */
public interface SmartAccountService {
    boolean isConfigured(String chainKey);

    String getOrDeployAccount(String chainKey, String ownerAddress);

    boolean hasSufficientSponsorshipQuota(String chainKey, String ownerAddress, BigDecimal estimatedCostUsd);

    void recordSponsorshipUsage(String chainKey, String ownerAddress, BigDecimal costUsd);

    void revokeSessionPermissions(String chainKey, String ownerAddress);

    List<SmartAccountInfo> discoverAccounts(String chainKey, String ownerAddress);

    SmartAccountInfo registerModularAccount(String chainKey, String ownerAddress, String accountAddress, String salt);

    String submitOwnerUserOperation(String chainKey, String ownerAddress, BundlerUserOperation operation);

    BundlerGasEstimate estimateUserOperationGas(String chainKey, BundlerUserOperation operation);

    Optional<AgentPermissionEpochInfo> getActivePermissionEpoch(String chainKey, String delegatorAddress);

    AgentPermissionEpochInfo recordPermissionEpochInstalled(
            String chainKey, String delegatorAddress, String agentAddress, String epoch,
            java.time.Instant validAfterUtc, java.time.Instant validBeforeUtc,
            String installedTxHash, List<AgentPermissionGrantInput> grants);
}

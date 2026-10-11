package dev.alexisreyna.artisanalbrew.application.service;

import java.util.Optional;

/**
 * Port of {@code IBundlerClient} in {@code Services/IBundlerClient.cs}.
 * Never calls {@code EntryPoint.handleOps} directly — only the bundler does.
 */
public interface BundlerClient {
    String sendUserOperation(String chainKey, BundlerUserOperation operation,
                             String entryPointOverride, String bundlerUrlOverride);

    default String sendUserOperation(String chainKey, BundlerUserOperation operation) {
        return sendUserOperation(chainKey, operation, null, null);
    }

    BundlerGasEstimate estimateUserOperationGas(String chainKey, BundlerUserOperation operation,
                                                String entryPointOverride, String bundlerUrlOverride);

    default BundlerGasEstimate estimateUserOperationGas(String chainKey, BundlerUserOperation operation) {
        return estimateUserOperationGas(chainKey, operation, null, null);
    }

    Optional<BundlerReceipt> getUserOperationReceipt(String chainKey, String userOperationHash, String bundlerUrlOverride);

    default Optional<BundlerReceipt> getUserOperationReceipt(String chainKey, String userOperationHash) {
        return getUserOperationReceipt(chainKey, userOperationHash, null);
    }
}

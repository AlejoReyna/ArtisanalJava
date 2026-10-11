package dev.alexisreyna.artisanalbrew.application.service;

/**
 * Port of {@code ISponsorshipPolicyService} in {@code Services/ISponsorshipPolicyService.cs}.
 * The policy — not the paymaster contract — is the safety boundary for sponsored gas.
 */
public interface SponsorshipPolicyService {
    SponsorshipDecision evaluate(SponsorshipRequest request);

    void recordUsage(SponsorshipRequest request);

    void revoke(String chainKey, String ownerAddress);

    /**
     * Meters a mined-but-reverted sponsored operation; revokes the grant once the
     * configured revert limit is reached.
     *
     * @return true when this revert caused revocation.
     */
    boolean recordRevertedOperation(String chainKey, String ownerAddress);
}

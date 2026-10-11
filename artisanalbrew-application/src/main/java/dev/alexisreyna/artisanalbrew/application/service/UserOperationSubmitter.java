package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code IUserOperationSubmitter} in {@code Services/IUserOperationSubmitter.cs}. */
public interface UserOperationSubmitter {
    UserOperationSubmissionResult submit(SponsoredUserOperation operation, SponsorshipSignature sponsorship, String signature);
}

package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code IUserOperationSponsor} in {@code Services/IUserOperationSponsor.cs}. */
public interface UserOperationSponsor {
    SponsorshipSignature sponsor(SponsoredUserOperation operation);
}

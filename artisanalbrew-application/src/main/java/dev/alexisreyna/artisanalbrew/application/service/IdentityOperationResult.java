package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code IdentityOperationResult} in {@code Services/IIdentityAccountService.cs}. */
public record IdentityOperationResult(boolean succeeded, String error) {

    public IdentityOperationResult(boolean succeeded) {
        this(succeeded, null);
    }
}

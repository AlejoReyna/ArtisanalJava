package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code UserOperationSubmissionStatus} in {@code Services/IUserOperationSubmitter.cs}. */
public enum UserOperationSubmissionStatus {
    DENIED,
    REJECTED,
    TIMED_OUT,
    REVERTED,
    CONFIRMED
}

package dev.alexisreyna.artisanalbrew.application.service;

import java.math.BigDecimal;

/** Port of {@code UserOperationSubmissionResult} in {@code Services/IUserOperationSubmitter.cs}. */
public record UserOperationSubmissionResult(
        UserOperationSubmissionStatus status,
        String detail,
        String userOperationHash,
        String transactionHash,
        BigDecimal costUsd) {

    public boolean confirmed() {
        return status == UserOperationSubmissionStatus.CONFIRMED;
    }

    public static UserOperationSubmissionResult denied(String detail) {
        return new UserOperationSubmissionResult(
                UserOperationSubmissionStatus.DENIED, detail, null, null, BigDecimal.ZERO);
    }
}

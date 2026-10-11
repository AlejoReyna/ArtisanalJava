package dev.alexisreyna.artisanalbrew.application.service.commerce;

/** Port of {@code EscrowEventType} in {@code AgenticCommerce/AgenticCommerceReconciliationApplicator.cs}. */
public enum EscrowEventType {
    JOB_CREATED,
    PROVIDER_SET,
    BUDGET_SET,
    JOB_FUNDED,
    JOB_SUBMITTED,
    JOB_COMPLETED,
    JOB_REJECTED,
    JOB_EXPIRED,
    PAYMENT_RELEASED,
    REFUNDED
}

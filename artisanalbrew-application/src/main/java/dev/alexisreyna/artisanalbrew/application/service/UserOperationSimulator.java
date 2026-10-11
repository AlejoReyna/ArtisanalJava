package dev.alexisreyna.artisanalbrew.application.service;

/** Port of {@code IUserOperationSimulator} in {@code Services/IUserOperationSimulator.cs}. */
public interface UserOperationSimulator {
    UserOperationSimulationResult simulate(UserOperationSimulationRequest request);
}

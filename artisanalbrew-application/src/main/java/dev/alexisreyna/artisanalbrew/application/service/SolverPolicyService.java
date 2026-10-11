package dev.alexisreyna.artisanalbrew.application.service;

import java.time.Instant;

/**
 * Port of {@code ISolverPolicyService} in {@code Services/ISolverPolicyService.cs}.
 * Fail-closed: refuses unless the pair is allowed, size fits, not expired, solver named.
 */
public interface SolverPolicyService {
    SolverPolicyDecision evaluate(SolverIntent intent, String solverAddress, Instant now);
}

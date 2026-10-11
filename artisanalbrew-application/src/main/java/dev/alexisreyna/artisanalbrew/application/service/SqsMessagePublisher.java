package dev.alexisreyna.artisanalbrew.application.service;

import java.util.Optional;

/** Port of {@code ISqsMessagePublisher} in {@code Services/ISqsMessagePublisher.cs}. */
public interface SqsMessagePublisher {
    Optional<String> publish(Object message);
}

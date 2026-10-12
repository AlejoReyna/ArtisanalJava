package dev.alexisreyna.artisanalbrew.infrastructure.service;

import dev.alexisreyna.artisanalbrew.application.service.SqsMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Unconfigured message publisher: logs and skips, mirroring
 * {@code SqsMessagePublisher}'s skip-with-warning when no Service Bus
 * namespace is configured. Registered by {@code UnconfiguredChainServices}
 * and backs off once a real transport bean exists.
 */
public class LoggingSqsMessagePublisher implements SqsMessagePublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingSqsMessagePublisher.class);

    @Override
    public Optional<String> publish(Object message) {
        log.warn("Message bus is not configured; dropping message of type {}",
                message == null ? "null" : message.getClass().getSimpleName());
        return Optional.empty();
    }
}

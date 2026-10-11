package dev.alexisreyna.artisanalbrew.application.validation;

/** Port of FluentValidation failures: thrown by hand-rolled validators in this module. */
public final class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

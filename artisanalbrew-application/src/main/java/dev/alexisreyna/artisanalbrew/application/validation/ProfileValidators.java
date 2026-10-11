package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.UpdateUserProfileRequest;

/** Port of {@code UpdateUserProfileRequestValidator} (cascade-stop order preserved). */
public final class ProfileValidators {
    private ProfileValidators() {
    }

    public static void validateUpdateDisplayName(UpdateUserProfileRequest request) {
        require(request != null, "Request is required.");
        String name = request.displayName();
        require(name != null && !name.isEmpty(), "Display name must not be empty.");
        require(name.trim().length() == name.length(), "Display name cannot include leading or trailing whitespace.");
        require(name.length() >= 2 && name.length() <= 160, "Display name must be 2-160 characters.");
        require(name.indexOf('<') < 0 && name.indexOf('>') < 0, "Display name cannot contain HTML tags.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}

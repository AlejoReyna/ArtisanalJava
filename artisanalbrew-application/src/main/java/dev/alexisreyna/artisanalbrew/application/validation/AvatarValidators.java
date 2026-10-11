package dev.alexisreyna.artisanalbrew.application.validation;

import dev.alexisreyna.artisanalbrew.application.dto.UpdateAvatarRequest;
import dev.alexisreyna.artisanalbrew.domain.avatars.AvatarCatalog;

/**
 * Port of {@code UpdateAvatarRequestValidator}.
 * Write path is strict where the read path is forgiving: unknown ids are rejected
 * here, silently repaired on read.
 */
public final class AvatarValidators {
    private AvatarValidators() {
    }

    public static void validateUpdate(UpdateAvatarRequest request) {
        require(request != null, "Request is required.");
        slot(request.backdrop(), AvatarCatalog.BACKDROP_SLOT);
        slot(request.chassis(), AvatarCatalog.CHASSIS_SLOT);
        slot(request.wear(), AvatarCatalog.WEAR_SLOT);
        slot(request.visor(), AvatarCatalog.VISOR_SLOT);
        slot(request.hat(), AvatarCatalog.HAT_SLOT);
        slot(request.hold(), AvatarCatalog.HOLD_SLOT);
    }

    private static void slot(String itemId, String slotKey) {
        var slot = AvatarCatalog.getSlot(slotKey);
        require(itemId != null && !itemId.isEmpty(), slot.getLabel() + " must not be empty.");
        require(slot.contains(itemId), "'" + itemId + "' is not one of the available " + slot.getLabel() + " options.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }
}

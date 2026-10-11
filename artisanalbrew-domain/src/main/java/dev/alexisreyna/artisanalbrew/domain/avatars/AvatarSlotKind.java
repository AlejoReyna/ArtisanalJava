package dev.alexisreyna.artisanalbrew.domain.avatars;

/**
 * Port of {@code AvatarSlotKind} in {@code ThisCafeteria.Domain/Avatars/AvatarCatalog.cs}.
 */
public enum AvatarSlotKind {
    /** One horizontal 64px sprite sheet per slot, indexed by {@link AvatarItem#sheetIndex()}. */
    SPRITE,
    /** No sprite; the item id keys a CSS colour behind the robot. */
    COLOUR
}

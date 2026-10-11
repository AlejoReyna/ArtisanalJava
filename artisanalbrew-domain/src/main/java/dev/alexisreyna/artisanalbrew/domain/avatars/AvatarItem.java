package dev.alexisreyna.artisanalbrew.domain.avatars;

/**
 * Port of {@code AvatarItem} in {@code ThisCafeteria.Domain/Avatars/AvatarCatalog.cs}.
 *
 * @param id stable storage key. Renaming one orphans every avatar wearing it.
 * @param label editor caption.
 * @param sheetIndex column in the slot's sprite sheet, or -1 for an item that draws
 *                   nothing (the "none" entry on optional slots).
 */
public record AvatarItem(String id, String label, int sheetIndex) {
    public boolean isEmpty() {
        return sheetIndex < 0;
    }
}

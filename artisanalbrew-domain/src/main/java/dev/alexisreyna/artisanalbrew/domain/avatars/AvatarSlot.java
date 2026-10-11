package dev.alexisreyna.artisanalbrew.domain.avatars;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of {@code AvatarSlot} in {@code ThisCafeteria.Domain/Avatars/AvatarCatalog.cs}.
 *
 * <p>.NET uses {@code FrozenDictionary}; Java uses an unmodifiable {@link Map} built once
 * in the constructor (same lookup semantics, {@code Ordinal} == case-sensitive).</p>
 */
public final class AvatarSlot {
    private final String key;
    private final String label;
    private final AvatarSlotKind kind;
    private final String defaultItemId;
    private final List<AvatarItem> items;
    private final int sheetFrames;
    private final AvatarItem defaultItem;
    private final Map<String, AvatarItem> byId;

    public AvatarSlot(String key, String label, AvatarSlotKind kind, String defaultItemId, List<AvatarItem> items) {
        this.key = key;
        this.label = label;
        this.kind = kind;
        this.defaultItemId = defaultItemId;
        this.items = List.copyOf(items);
        this.sheetFrames = (int) this.items.stream().filter(i -> !i.isEmpty()).count();
        Map<String, AvatarItem> map = new LinkedHashMap<>();
        for (AvatarItem item : this.items) {
            map.put(item.id(), item);
        }
        this.byId = Collections.unmodifiableMap(map);
        AvatarItem def = this.byId.get(defaultItemId);
        if (def == null) {
            throw new IllegalStateException(
                    "Avatar slot '" + key + "' names default item '" + defaultItemId + "', which is not one of its items.");
        }
        this.defaultItem = def;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public AvatarSlotKind getKind() {
        return kind;
    }

    public String getDefaultItemId() {
        return defaultItemId;
    }

    public List<AvatarItem> getItems() {
        return items;
    }

    /** Columns in this slot's sheet — what CSS needs for {@code background-size}. */
    public int getSheetFrames() {
        return sheetFrames;
    }

    public AvatarItem getDefault() {
        return defaultItem;
    }

    public AvatarItem resolve(String itemId) {
        if (itemId != null) {
            AvatarItem item = byId.get(itemId);
            if (item != null) {
                return item;
            }
        }
        return defaultItem;
    }

    public boolean contains(String itemId) {
        return itemId != null && byId.containsKey(itemId);
    }
}

package dev.alexisreyna.artisanalbrew.domain.avatars;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of {@code ThisCafeteria.Domain/Avatars/AvatarCatalog.cs}.
 *
 * <p>Single source of truth for what a robot can wear. Three consumers must not drift:
 * the sprite generator ({@code tools/generate_avatar_parts.py}), the editor grid built
 * from {@link #getSlots()}, and {@link #normalize(RobotAvatar)} on the read path.</p>
 */
public final class AvatarCatalog {
    public static final String CHASSIS_SLOT = "chassis";
    public static final String VISOR_SLOT = "visor";
    public static final String HAT_SLOT = "hat";
    public static final String WEAR_SLOT = "wear";
    public static final String HOLD_SLOT = "hold";
    public static final String BACKDROP_SLOT = "backdrop";

    /** The id every optional slot uses for "wearing nothing here". */
    public static final String NONE_ITEM_ID = "none";

    /** Every sprite sheet is this many pixels per frame, square. */
    public static final int FRAME_SIZE = 64;

    /** Layer order, back to front — also the order the editor lists its tabs. */
    private static final List<AvatarSlot> SLOTS = List.of(
            new AvatarSlot(BACKDROP_SLOT, "Backdrop", AvatarSlotKind.COLOUR, "roast", List.of(
                    new AvatarItem("roast", "Roast", 0),
                    new AvatarItem("espresso", "Espresso", 1),
                    new AvatarItem("teal", "Teal", 2),
                    new AvatarItem("copper", "Copper", 3),
                    new AvatarItem("clay", "Clay", 4),
                    new AvatarItem("moss", "Moss", 5),
                    new AvatarItem("night", "Night", 6),
                    new AvatarItem("gold", "Gold", 7))),
            new AvatarSlot(CHASSIS_SLOT, "Chassis", AvatarSlotKind.SPRITE, "cream", List.of(
                    new AvatarItem("cream", "Cream", 0),
                    new AvatarItem("teal", "Teal", 1),
                    new AvatarItem("copper", "Copper", 2),
                    new AvatarItem("clay", "Clay", 3),
                    new AvatarItem("moss", "Moss", 4),
                    new AvatarItem("graphite", "Graphite", 5))),
            new AvatarSlot(WEAR_SLOT, "Uniform", AvatarSlotKind.SPRITE, NONE_ITEM_ID, List.of(
                    new AvatarItem(NONE_ITEM_ID, "None", -1),
                    new AvatarItem("apron", "Apron", 0),
                    new AvatarItem("hivis", "Hi-vis", 1),
                    new AvatarItem("scarf", "Scarf", 2),
                    new AvatarItem("bowtie", "Bow tie", 3),
                    new AvatarItem("labcoat", "Lab coat", 4),
                    new AvatarItem("bandolier", "Bandolier", 5))),
            new AvatarSlot(VISOR_SLOT, "Visor", AvatarSlotKind.SPRITE, "dot", List.of(
                    new AvatarItem("dot", "Dot", 0),
                    new AvatarItem("happy", "Happy", 1),
                    new AvatarItem("sleepy", "Sleepy", 2),
                    new AvatarItem("shades", "Shades", 3),
                    new AvatarItem("scanline", "Scanline", 4),
                    new AvatarItem("glitch", "Glitch", 5))),
            new AvatarSlot(HAT_SLOT, "Headgear", AvatarSlotKind.SPRITE, NONE_ITEM_ID, List.of(
                    new AvatarItem(NONE_ITEM_ID, "None", -1),
                    new AvatarItem("beanie", "Beanie", 0),
                    new AvatarItem("barista", "Barista cap", 1),
                    new AvatarItem("hardhat", "Hard hat", 2),
                    new AvatarItem("headphones", "Headphones", 3),
                    new AvatarItem("crown", "Crown", 4),
                    new AvatarItem("bulb", "Antenna bulb", 5),
                    new AvatarItem("toque", "Toque", 6))),
            new AvatarSlot(HOLD_SLOT, "Holding", AvatarSlotKind.SPRITE, NONE_ITEM_ID, List.of(
                    new AvatarItem(NONE_ITEM_ID, "None", -1),
                    new AvatarItem("mug", "Mug", 0),
                    new AvatarItem("beanbag", "Bean bag", 1),
                    new AvatarItem("key", "Key", 2),
                    new AvatarItem("wrench", "Wrench", 3),
                    new AvatarItem("terminal", "Terminal", 4),
                    new AvatarItem("coin", "CAFE coin", 5))));

    private static final Map<String, AvatarSlot> SLOTS_BY_KEY;

    static {
        Map<String, AvatarSlot> map = new LinkedHashMap<>();
        for (AvatarSlot slot : SLOTS) {
            map.put(slot.getKey(), slot);
        }
        SLOTS_BY_KEY = Collections.unmodifiableMap(map);
    }

    private AvatarCatalog() {
    }

    public static List<AvatarSlot> getSlots() {
        return SLOTS;
    }

    public static AvatarSlot getSlot(String slotKey) {
        AvatarSlot slot = SLOTS_BY_KEY.get(slotKey);
        if (slot == null) {
            throw new IllegalArgumentException("Unknown avatar slot: " + slotKey);
        }
        return slot;
    }

    public static boolean isKnown(String slotKey, String itemId) {
        AvatarSlot slot = SLOTS_BY_KEY.get(slotKey);
        return slot != null && slot.contains(itemId);
    }

    public static RobotAvatar createDefault() {
        RobotAvatar avatar = new RobotAvatar();
        for (AvatarSlot slot : SLOTS) {
            avatar.set(slot.getKey(), slot.getDefaultItemId());
        }
        return avatar;
    }

    /**
     * Replaces every unknown or blank item id with its slot default.
     *
     * <p>Always returns a new instance. Normalising in place would mutate the tracked
     * entity during a <em>read</em>, so a later unrelated save would quietly rewrite
     * the row with fallback values — same rationale as the .NET comment.</p>
     */
    public static RobotAvatar normalize(RobotAvatar avatar) {
        RobotAvatar normalized = new RobotAvatar();
        for (AvatarSlot slot : SLOTS) {
            String stored = avatar == null ? null : avatar.get(slot.getKey());
            String id = (stored == null || stored.isBlank()) ? null : stored;
            normalized.set(slot.getKey(), slot.resolve(id).id());
        }
        return normalized;
    }
}

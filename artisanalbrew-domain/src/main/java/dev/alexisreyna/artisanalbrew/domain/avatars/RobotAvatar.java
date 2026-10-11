package dev.alexisreyna.artisanalbrew.domain.avatars;

/**
 * Port of {@code ThisCafeteria.Domain/Avatars/RobotAvatar.cs}.
 *
 * <p>Stored as a single JSON column (Phase 3) rather than six columns, mirroring .NET.
 * Item ids are untrusted: always render via {@link AvatarCatalog#normalize(RobotAvatar)}.</p>
 */
public class RobotAvatar {
    private String chassis = "";
    private String visor = "";
    private String hat = "";
    private String wear = "";
    private String hold = "";
    private String backdrop = "";

    public String getChassis() {
        return chassis;
    }

    public void setChassis(String chassis) {
        this.chassis = chassis;
    }

    public String getVisor() {
        return visor;
    }

    public void setVisor(String visor) {
        this.visor = visor;
    }

    public String getHat() {
        return hat;
    }

    public void setHat(String hat) {
        this.hat = hat;
    }

    public String getWear() {
        return wear;
    }

    public void setWear(String wear) {
        this.wear = wear;
    }

    public String getHold() {
        return hold;
    }

    public void setHold(String hold) {
        this.hold = hold;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
    }

    /**
     * Slot-keyed access so the editor/renderer loop over {@link AvatarCatalog#getSlots()}.
     *
     * @throws IllegalArgumentException unknown slot key (coding mistake — fails loudly,
     *                                  unlike unknown <em>item ids</em> which fall back silently).
     */
    public String get(String slotKey) {
        return switch (slotKey) {
            case AvatarCatalog.CHASSIS_SLOT -> chassis;
            case AvatarCatalog.VISOR_SLOT -> visor;
            case AvatarCatalog.HAT_SLOT -> hat;
            case AvatarCatalog.WEAR_SLOT -> wear;
            case AvatarCatalog.HOLD_SLOT -> hold;
            case AvatarCatalog.BACKDROP_SLOT -> backdrop;
            default -> throw new IllegalArgumentException("Unknown avatar slot: " + slotKey);
        };
    }

    public void set(String slotKey, String value) {
        switch (slotKey) {
            case AvatarCatalog.CHASSIS_SLOT -> chassis = value;
            case AvatarCatalog.VISOR_SLOT -> visor = value;
            case AvatarCatalog.HAT_SLOT -> hat = value;
            case AvatarCatalog.WEAR_SLOT -> wear = value;
            case AvatarCatalog.HOLD_SLOT -> hold = value;
            case AvatarCatalog.BACKDROP_SLOT -> backdrop = value;
            default -> throw new IllegalArgumentException("Unknown avatar slot: " + slotKey);
        }
    }

    public RobotAvatar clone() {
        RobotAvatar copy = new RobotAvatar();
        copy.chassis = this.chassis;
        copy.visor = this.visor;
        copy.hat = this.hat;
        copy.wear = this.wear;
        copy.hold = this.hold;
        copy.backdrop = this.backdrop;
        return copy;
    }

    public boolean hasSameLook(RobotAvatar other) {
        if (other == null) {
            return false;
        }
        return eq(chassis, other.chassis)
                && eq(visor, other.visor)
                && eq(hat, other.hat)
                && eq(wear, other.wear)
                && eq(hold, other.hold)
                && eq(backdrop, other.backdrop);
    }

    private static boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}

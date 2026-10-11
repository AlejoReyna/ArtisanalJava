package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.avatars.AvatarCatalog;
import dev.alexisreyna.artisanalbrew.domain.avatars.AvatarSeed;
import dev.alexisreyna.artisanalbrew.domain.avatars.RobotAvatar;

/**
 * Port of {@code RobotAvatarDto} in {@code DTOs/RobotAvatarDto.cs}.
 * Every route from the database to a screen goes through {@link #resolve}.
 */
public record RobotAvatarDto(
        String backdrop,
        String chassis,
        String wear,
        String visor,
        String hat,
        String hold) {

    public static RobotAvatarDto resolve(RobotAvatar stored, String walletAddress) {
        RobotAvatar look = AvatarCatalog.normalize(stored != null ? stored : AvatarSeed.fromWallet(walletAddress));
        return new RobotAvatarDto(
                look.getBackdrop(), look.getChassis(), look.getWear(),
                look.getVisor(), look.getHat(), look.getHold());
    }

    public String get(String slotKey) {
        return switch (slotKey) {
            case AvatarCatalog.BACKDROP_SLOT -> backdrop;
            case AvatarCatalog.CHASSIS_SLOT -> chassis;
            case AvatarCatalog.WEAR_SLOT -> wear;
            case AvatarCatalog.VISOR_SLOT -> visor;
            case AvatarCatalog.HAT_SLOT -> hat;
            case AvatarCatalog.HOLD_SLOT -> hold;
            default -> throw new IllegalArgumentException("Unknown avatar slot: " + slotKey);
        };
    }
}

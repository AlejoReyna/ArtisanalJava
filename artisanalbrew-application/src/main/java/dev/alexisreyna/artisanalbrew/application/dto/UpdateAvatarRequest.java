package dev.alexisreyna.artisanalbrew.application.dto;

import dev.alexisreyna.artisanalbrew.domain.avatars.RobotAvatar;

/**
 * Port of {@code UpdateAvatarRequest} in {@code DTOs/UpdateAvatarRequest.cs}.
 * Every slot required; removing a piece is {@code "none"}, not omission.
 */
public record UpdateAvatarRequest(
        String backdrop,
        String chassis,
        String wear,
        String visor,
        String hat,
        String hold) {

    public RobotAvatar toRobotAvatar() {
        RobotAvatar avatar = new RobotAvatar();
        avatar.setBackdrop(backdrop);
        avatar.setChassis(chassis);
        avatar.setWear(wear);
        avatar.setVisor(visor);
        avatar.setHat(hat);
        avatar.setHold(hold);
        return avatar;
    }
}

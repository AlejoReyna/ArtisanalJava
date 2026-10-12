package dev.alexisreyna.artisanalbrew.infrastructure.persistence.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexisreyna.artisanalbrew.domain.avatars.RobotAvatar;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON codec for the {@code UserProfiles."Avatar"} column.
 * .NET owns the avatar as one {@code jsonb} object with PascalCase keys;
 * this port stores the same shape as TEXT (see V1 header). Key order and
 * names match so rows stay interchangeable.
 */
public final class AvatarJson {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AvatarJson() {
    }

    public static String toJson(RobotAvatar avatar) {
        if (avatar == null) {
            return null;
        }
        Map<String, String> json = new LinkedHashMap<>();
        json.put("Chassis", avatar.getChassis());
        json.put("Visor", avatar.getVisor());
        json.put("Hat", avatar.getHat());
        json.put("Wear", avatar.getWear());
        json.put("Hold", avatar.getHold());
        json.put("Backdrop", avatar.getBackdrop());
        try {
            return MAPPER.writeValueAsString(json);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize avatar", e);
        }
    }

    public static RobotAvatar fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> map = MAPPER.readValue(json, Map.class);
            RobotAvatar avatar = new RobotAvatar();
            avatar.setChassis(str(map.get("Chassis")));
            avatar.setVisor(str(map.get("Visor")));
            avatar.setHat(str(map.get("Hat")));
            avatar.setWear(str(map.get("Wear")));
            avatar.setHold(str(map.get("Hold")));
            avatar.setBackdrop(str(map.get("Backdrop")));
            return avatar;
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize avatar", e);
        }
    }

    private static String str(Object value) {
        return value == null ? "" : value.toString();
    }
}

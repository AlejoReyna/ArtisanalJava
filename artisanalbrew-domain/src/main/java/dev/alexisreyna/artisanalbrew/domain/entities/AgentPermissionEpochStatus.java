package dev.alexisreyna.artisanalbrew.domain.entities;

/** Port of {@code AgentPermissionEpochStatus} in {@code AgentPermissionEpoch.cs}. */
public enum AgentPermissionEpochStatus {
    PENDING_ACTIVATION(0),
    ACTIVE(1),
    EXPIRED(2),
    REVOKED(3);

    private final int code;

    AgentPermissionEpochStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AgentPermissionEpochStatus fromCode(int code) {
        for (AgentPermissionEpochStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown AgentPermissionEpochStatus code: " + code);
    }
}

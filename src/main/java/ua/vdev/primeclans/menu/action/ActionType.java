package ua.vdev.primeclans.menu.action;

import java.util.Arrays;
import java.util.Optional;

public enum ActionType {
    PLAY_SOUND("[sound]"),
    CLOSE("[close]"),
    MESSAGE("[message]"),
    OPEN_MENU("[open_menu]"),
    CREATE_CLAN("[create_clan]"),
    DELETE_CLAN("[delete_clan]"),
    PLAYER_COMMAND("[player]"),
    CONSOLE_COMMAND("[console]"),
    TITLE("[title]"),
    ACTIONBAR("[actionbar]"),
    PLAYER_EFFECT("[effect]"),
    ENABLE_GLOW("[enable-glow]"),
    DISABLE_GLOW("[disable-glow]"),
    SET_GLOW_COLOR("[set-glow-color]"),
    SET_MEMBER_GLOW_COLOR("[set-member-glow-color]"),
    RESET_MEMBER_GLOW_COLOR("[reset-member-glow-color]"),
    TOGGLE_PERM("[toggle-perm]");

    private final String prefix;

    ActionType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public static Optional<ActionType> fromRaw(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String lower = raw.toLowerCase().trim();
        return Arrays.stream(values())
                .filter(type -> lower.startsWith(type.prefix.toLowerCase()))
                .findFirst();
    }
}
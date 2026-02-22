package ua.vdev.primeclans.menu;

public enum MenuType {
    CONFIRM_CREATE("confirm-create"),
    CONFIRM_DELETE("confirm-delete"),
    MAIN_MENU("main-menu"),
    PLAYER_LIST("player-list"),
    GLOW_MENU("glow"),
    PLAYER_GLOW_LIST("player-glow-list"),
    PLAYER_GLOW_COLOR("player-glow-color"),
    PLAYER_PERM("player-perm");

    private final String configName;
    MenuType(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }
}
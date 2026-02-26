package ua.vdev.primeclans.perm;

/**
 * @param key уникальный строковый ключ в ВЕРХНЕМ регистре (например, "KICK_MEMBERS", "HOME_SET")
 * @param configKey короткий ключ для YAML-плейсхолдеров в нижнем регистре (например, "kick", "home_set")
 * используется в {@code {perm_<configKey>_material}} и {@code {perm_<configKey>_status}}
 * @param displayName отображаемое название в UI (поддерживает MiniMessage)
 * @param description краткое описание права (поддерживает MiniMessage)
 */
public record ClanPermEntry(String key, String configKey, String displayName, String description) {

    /**
     * Создаёт запись автоматически приводя key к верхнему регистру
     * а configKey к нижнему
     */
    public static ClanPermEntry of(String key, String configKey, String displayName, String description) {
        return new ClanPermEntry(
                key.toUpperCase(),
                configKey.toLowerCase(),
                displayName,
                description
        );
    }

    /** Создаёт запись без описания */
    public static ClanPermEntry of(String key, String configKey, String displayName) {
        return of(key, configKey, displayName, "");
    }
}
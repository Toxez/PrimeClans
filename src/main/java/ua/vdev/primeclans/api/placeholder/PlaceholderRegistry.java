package ua.vdev.primeclans.api.placeholder;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderRegistry {

    private static final Map<String, ClanPlaceholder> placeholders = new HashMap<>();

    public static void register(String key, ClanPlaceholder placeholder) {
        placeholders.put(key.toLowerCase(), placeholder);
    }

    public static void unregister(String key) {
        placeholders.remove(key.toLowerCase());
    }

    public static Map<String, String> resolve(Clan clan, Player player) {
        if (placeholders.isEmpty()) return Collections.emptyMap();
        Map<String, String> result = new HashMap<>();
        placeholders.forEach((key, ph) -> {
            try {
                result.put(key, ph.resolve(clan, player));
            } catch (Exception e) {
                result.put(key, "");
            }
        });
        return result;
    }

    public static Map<String, ClanPlaceholder> getAll() {
        return Collections.unmodifiableMap(placeholders);
    }
}
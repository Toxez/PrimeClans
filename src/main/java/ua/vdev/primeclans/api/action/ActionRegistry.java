package ua.vdev.primeclans.api.action;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ActionRegistry {

    private static final Map<String, AddonAction> actions = new LinkedHashMap<>();

    public static void register(String prefix, AddonAction action) {
        actions.put(prefix.toLowerCase(), action);
    }

    public static void unregister(String prefix) {
        actions.remove(prefix.toLowerCase());
    }

    public static Optional<AddonActionMatch> findMatch(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String lower = raw.toLowerCase().trim();
        for (Map.Entry<String, AddonAction> entry : actions.entrySet()) {
            if (lower.startsWith(entry.getKey())) {
                String arg = raw.substring(entry.getKey().length()).trim();
                return Optional.of(new AddonActionMatch(entry.getValue(), arg));
            }
        }
        return Optional.empty();
    }

    public record AddonActionMatch(AddonAction action, String arg) {}
}
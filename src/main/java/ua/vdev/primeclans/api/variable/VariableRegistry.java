package ua.vdev.primeclans.api.variable;

import ua.vdev.primeclans.model.Clan;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableRegistry {

    private static final Map<String, ContextVariable> variables = new ConcurrentHashMap<>();
    private static List<String> sortedKeys = new ArrayList<>();

    static {
        registerInternal("in_clan", (player, clan) -> String.valueOf(clan != null));
        registerInternal("is_owner", (player, clan) -> String.valueOf(clan != null && clan.isOwner(player.getUniqueId())));
        registerInternal("clan_balance", (player, clan) -> clan != null ? String.valueOf(clan.balance()) : "0");
        registerInternal("clan_level", (player, clan) -> clan != null ? String.valueOf(clan.level()) : "0");
        registerInternal("clan_exp", (player, clan) -> clan != null ? String.valueOf(clan.exp()) : "0");
        registerInternal("clan_members_count", (player, clan) -> clan != null ? String.valueOf(clan.members().size()) : "0");
    }

    @ApiStatus.Internal
    public static void register(String name, ContextVariable variable) {
        registerInternal(name, variable);
    }

    private static synchronized void registerInternal(String name, ContextVariable variable) {
        variables.put(name.toLowerCase(), variable);

        List<String> keys = new ArrayList<>(variables.keySet());
        keys.sort((a, b) -> Integer.compare(b.length(), a.length()));
        sortedKeys = keys;
    }

    public static Map<String, ContextVariable> getAll() {
        return Collections.unmodifiableMap(variables);
    }

    public static String replaceAll(String text, Player player, Clan clan) {
        if (text == null || text.isBlank()) return text;

        String result = text;
        for (String key : sortedKeys) {
            String resolvedValue = variables.get(key).resolve(player, clan);
            if (resolvedValue == null) resolvedValue = "null";
            result = result.replaceAll("(?i)\\b" + Pattern.quote(key) + "\\b", Matcher.quoteReplacement(resolvedValue));
        }
        return result;
    }
}
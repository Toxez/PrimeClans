package ua.vdev.primeclans.api.requirement;

import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.perm.ClanPerm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequirementRegistry {

    private static final Map<String, AddonRequirement> requirements = new HashMap<>();

    static {
        register("HAS_MONEY", (player, clan, params) -> {
            double amount = getDouble(params, "amount", 0);
            return PrimeClans.getInstance().getEconomyManager().has(player, amount);
        });

        register("CLAN_BALANCE", (player, clan, params) -> {
            if (clan == null) return false;
            double min = getDouble(params, "min", 0);
            return clan.balance() >= min;
        });

        register("CLAN_LEVEL", (player, clan, params) -> {
            if (clan == null) return false;
            int min = getInt(params, "min", 1);
            int max = getInt(params, "max", Integer.MAX_VALUE);
            return clan.level() >= min && clan.level() <= max;
        });

        register("PLAYER_LEVEL", (player, clan, params) -> {
            int min = getInt(params, "min", 0);
            return player.getLevel() >= min;
        });

        register("IN_CLAN", (player, clan, params) -> clan != null);

        register("NOT_IN_CLAN", (player, clan, params) -> clan == null);

        register("IS_OWNER", (player, clan, params) -> {
            if (clan == null) return false;
            return clan.isOwner(player.getUniqueId());
        });

        register("CLAN_MEMBERS", (player, clan, params) -> {
            if (clan == null) return false;
            int min = getInt(params, "min", 0);
            int max = getInt(params, "max", Integer.MAX_VALUE);
            int size = clan.members().size();
            return size >= min && size <= max;
        });

        register("HAS_PERM", (player, clan, params) -> {
            if (clan == null) return false;
            String permName = getString(params, "perm", "");
            return ClanPerm.of(permName)
                    .map(perm -> clan.hasPerm(player.getUniqueId(), perm))
                    .orElse(false);
        });
    }

    public static void register(String type, AddonRequirement requirement) {
        requirements.put(type.toUpperCase(), requirement);
    }

    public static Optional<AddonRequirement> get(String type) {
        return Optional.ofNullable(requirements.get(type.toUpperCase()));
    }

    public static Map<String, AddonRequirement> getAll() {
        return Collections.unmodifiableMap(requirements);
    }

    private static double getDouble(Map<String, Object> params, String key, double def) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return def;
    }

    private static int getInt(Map<String, Object> params, String key, int def) {
        Object val = params.get(key);
        if (val instanceof Number n) return n.intValue();
        return def;
    }

    private static String getString(Map<String, Object> params, String key, String def) {
        Object val = params.get(key);
        return val != null ? val.toString() : def;
    }
}
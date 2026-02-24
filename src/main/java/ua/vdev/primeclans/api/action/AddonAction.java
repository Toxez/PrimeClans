package ua.vdev.primeclans.api.action;

import org.bukkit.entity.Player;

import java.util.Map;

@FunctionalInterface
public interface AddonAction {
    /**
     * @param player игрок который кликнул
     * @param arg аргумент после префикса [action] <arg>
     * @param context контекст clan_name target_uuid placeholders
     */
    void execute(Player player, String arg, Map<String, Object> context);
}
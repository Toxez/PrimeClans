package ua.vdev.primeclans.api.command;

import org.bukkit.entity.Player;

import java.util.List;

public interface AddonSubCommand {
    String getName();
    void execute(Player player, String[] args);

    default List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }

    /**
     * true = показывать только если игрок В клане
     */
    default boolean requiresClan() {
        return true;
    }
    /**
     * true = показывать только если игрок НЕ в клане
     */
    default boolean requiresNoClan() {
        return false;
    }
}
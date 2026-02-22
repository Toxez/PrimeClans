package ua.vdev.primeclans.menu.action;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface MenuAction {
    void execute(Player player);
}
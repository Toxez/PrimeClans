package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.action.MenuAction;

public class Close implements MenuAction {
    @Override
    public void execute(Player player) {
        player.closeInventory();
        MenuManager.closeMenu(player);
    }
}
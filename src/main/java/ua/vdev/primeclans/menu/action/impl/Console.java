package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.MenuAction;

public class Console implements MenuAction {
    private final String command;
    public Console(String command) {
        this.command = command;
    }

    @Override
    public void execute(Player player) {
        if (command == null || command.isEmpty()) return;
        String cmd = command.startsWith("/")
                ? command.substring(1)
                : command;
        String processed = cmd
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processed);
    }
}
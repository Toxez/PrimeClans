package ua.vdev.primeclans.menu.action.impl;

import ua.vdev.primeclans.menu.action.MenuAction;

public class Player implements MenuAction {
    private final String command;

    public Player(String command) {
        this.command = command;
    }

    @Override
    public void execute(org.bukkit.entity.Player player) {
        if (command == null || command.isEmpty()) return;
        String cmd = command.startsWith("/")
                ? command.substring(1)
                : command;

        player.performCommand(cmd);
    }
}
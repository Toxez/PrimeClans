package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;

public class DeleteClan implements MenuAction {
    private final String clanName;

    public DeleteClan(String clanName) {
        this.clanName = clanName;
    }

    @Override
    public void execute(Player player) {
        PrimeClans.getInstance().getClanManager().deleteClan(clanName);
        Lang.send(player, "delete.deleted", Map.of("clan", clanName));
        player.closeInventory();
    }
}
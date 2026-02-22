package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;

public class CreateClan implements MenuAction {
    private final String clanName;
    public CreateClan(String clanName) {
        this.clanName = clanName;
    }

    @Override
    public void execute(Player player) {
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();
        double cost = PrimeClans.getInstance().getConfig().getDouble("settings.create-cost", 0.0);
        if (cost > 0 && !economy.has(player, cost)) {
            Lang.send(player, "create.no-money", Map.of(
                    "cost", economy.format(cost),
                    "balance", economy.format(economy.getBalance(player))
            ));
            player.closeInventory();
            return;
        }

        if (cost > 0) {
            economy.withdraw(player, cost);
        }

        PrimeClans.getInstance().getClanManager().createClan(clanName, player.getUniqueId());
        Lang.send(player, "create.created", Map.of("clan", clanName));
        player.closeInventory();
    }
}
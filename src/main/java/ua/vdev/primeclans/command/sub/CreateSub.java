package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.impl.ConfirmCreate;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CreateSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        Optional.of(player.getUniqueId())
                .flatMap(clanManager::getPlayerClan)
                .ifPresentOrElse(
                        clan -> Lang.send(player, "create.already-in-clan"),
                        () -> validateAndCreateClan(player, args)
                );
    }

    private void validateAndCreateClan(Player player, String[] args) {
        Optional.of(args)
                .filter(a -> a.length >= 2)
                .map(a -> a[1])
                .ifPresentOrElse(
                        clanName -> validateClanName(player, clanName),
                        () -> Lang.send(player, "create.usage")
                );
    }

    private void validateClanName(Player player, String clanName) {
        int min = PrimeClans.getInstance().getConfig().getInt("settings.min-name", 3);
        int max = PrimeClans.getInstance().getConfig().getInt("settings.max-name", 8);
        Optional.of(clanName)
                .filter(name -> name.length() >= min && name.length() <= max)
                .ifPresentOrElse(
                        validName -> checkClanExists(player, validName),
                        () -> Lang.send(player, "create.invalid-length", Map.of("min", String.valueOf(min), "max", String.valueOf(max)))
                );
    }

    private void checkClanExists(Player player, String clanName) {
        Optional.of(clanName)
                .filter(name -> !clanManager.exists(name))
                .ifPresentOrElse(
                        validName -> openConfirmMenu(player, validName),
                        () -> Lang.send(player, "create.already-exists", Map.of("clan", clanName))
                );
    }

    private void openConfirmMenu(Player player, String clanName) {
        double cost = PrimeClans.getInstance().getConfig().getDouble("settings.create-cost", 0.0);
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();

        if (cost > 0 && !economy.has(player, cost)) {
            Lang.send(player, "create.no-money",
                    Map.of(
                            "cost", economy.format(cost),
                            "balance", economy.format(economy.getBalance(player))
                    ));
            return;
        }

        MenuManager.openMenu(player, new ConfirmCreate(clanName));
    }

    @Override
    public String getName() {return "create";}
}
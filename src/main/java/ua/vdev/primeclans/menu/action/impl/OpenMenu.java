package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.impl.GlowMenu;
import ua.vdev.primeclans.menu.impl.MainMenu;
import ua.vdev.primeclans.menu.impl.PlayerGlowList;
import ua.vdev.primeclans.menu.impl.PlayerList;
import ua.vdev.vlibapi.player.PlayerMsg;

import java.util.Optional;

public class OpenMenu implements MenuAction {
    private final String menuId;

    public OpenMenu(String menuId) {
        this.menuId = menuId;
    }

    @Override
    public void execute(Player player) {
        createMenuById(player, menuId)
                .ifPresentOrElse(
                        menu -> MenuManager.openMenu(player, menu),
                        () -> PlayerMsg.send(player, "<red>Меню <gold>" + menuId + " <red>не найдено")
                );
    }

    private Optional<Menu> createMenuById(Player player, String id) {
        String lower = id.toLowerCase();
        return switch (lower) {
            case "main-menu" -> PrimeClans.getInstance().getClanManager()
                    .getPlayerClan(player.getUniqueId())
                    .map(MainMenu::new);
            case "player-list" -> PrimeClans.getInstance().getClanManager()
                    .getPlayerClan(player.getUniqueId())
                    .map(PlayerList::new);
            case "glow" -> PrimeClans.getInstance().getClanManager()
                    .getPlayerClan(player.getUniqueId())
                    .map(GlowMenu::new);
            case "player-glow-list" -> PrimeClans.getInstance().getClanManager()
                    .getPlayerClan(player.getUniqueId())
                    .map(PlayerGlowList::new);
            default -> Optional.empty();
        };
    }
}
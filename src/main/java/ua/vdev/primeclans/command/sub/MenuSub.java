package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.impl.MainMenu;
import ua.vdev.primeclans.util.Lang;

@RequiredArgsConstructor
public class MenuSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId())
                .ifPresentOrElse(
                        clan -> MenuManager.openMenu(player, new MainMenu(clan)),
                        () -> Lang.send(player, "menu.no-clan")
                );
    }

    @Override
    public String getName() {return "menu";}
}
package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.impl.ConfirmDelete;
import ua.vdev.primeclans.util.Lang;

@RequiredArgsConstructor
public class DeleteSub implements SubCommand {
    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.isOwner(player.getUniqueId())) {
                Lang.send(player, "delete.not-owner");
                return;
            }

            MenuManager.openMenu(player, new ConfirmDelete(clan.name()));

        }, () -> Lang.send(player, "help.no-clan"));
    }

    @Override
    public String getName() {return "delete";}
}
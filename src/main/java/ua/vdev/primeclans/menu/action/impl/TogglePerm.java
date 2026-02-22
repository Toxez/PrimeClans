package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.impl.PlayerPerm;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.util.UUID;

public class TogglePerm implements MenuAction {

    private final UUID targetUuid;
    private final ClanPerm perm;

    public TogglePerm(UUID targetUuid, ClanPerm perm) {
        this.targetUuid = targetUuid;
        this.perm = perm;
    }

    @Override
    public void execute(Player player) {
        ClanManager cm = PrimeClans.getInstance().getClanManager();

        cm.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.isOwner(player.getUniqueId())) {
                Lang.send(player, "perm.not-leader");
                return;
            }
            if (!clan.members().contains(targetUuid)) {
                Lang.send(player, "perm.target-not-in-clan");
                return;
            }
            if (clan.isOwner(targetUuid)) {
                Lang.send(player, "perm.cannot-change-leader");
                return;
            }

            cm.toggleMemberPerm(clan.name(), targetUuid, perm).ifPresent(granted ->
                    Task.sync(() ->
                            cm.getPlayerClan(player.getUniqueId()).ifPresent(updatedClan ->
                                    MenuManager.openMenu(player, new PlayerPerm(updatedClan, targetUuid))
                            )
                    )
            );
        }, () -> Lang.send(player, "perm.no-clan"));
    }
}
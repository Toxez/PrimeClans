package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Map;
import java.util.UUID;

public class ResetMemberGlowColor implements MenuAction {
    private final UUID targetUuid;

    public ResetMemberGlowColor(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    @Override
    public void execute(Player player) {
        ClanManager cm = PrimeClans.getInstance().getClanManager();

        cm.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.hasPerm(player.getUniqueId(), ClanPerm.MANAGE_MEMBER_GLOW)) {
                Lang.send(player, "perm.no-perm");
                return;
            }

            if (!clan.members().contains(targetUuid)) {
                Lang.send(player, "perm.target-not-in-clan");
                return;
            }

            cm.resetMemberGlowColor(clan.name(), targetUuid);
            String targetName = PlayerFind.uuid(targetUuid)
                    .map(Player::getName)
                    .orElseGet(() -> Bukkit.getOfflinePlayer(targetUuid).getName());

            if (targetName == null) targetName = targetUuid.toString();
            Lang.send(player, "glow.member-color-reset", Map.of("player", targetName));

            player.updateInventory();
        }, () -> Lang.send(player, "glow.no-clan"));
    }
}
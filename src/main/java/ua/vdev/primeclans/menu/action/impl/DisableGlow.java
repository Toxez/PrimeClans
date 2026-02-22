package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.glow.manager.GlowManager;
import ua.vdev.primeclans.glow.manager.GlowUpdater;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Map;
import java.util.Optional;

public class DisableGlow implements MenuAction {

    @Override
    public void execute(Player player) {
        ClanManager clanManager = PrimeClans.getInstance().getClanManager();
        clanManager.getPlayerClan(player.getUniqueId())
                .ifPresentOrElse(
                        clan -> processDisableGlow(player, clan),
                        () -> Lang.send(player, "glow.no-clan")
                );
    }

    private void processDisableGlow(Player player, Clan clan) {
        if (!clan.hasPerm(player.getUniqueId(), ClanPerm.MANAGE_GLOW)) {
            Lang.send(player, "perm.no-perm");
            return;
        }
        Map<String, String> placeholders = Map.of("clan", clan.name());
        clan.members().stream()
                .map(PlayerFind::uuid)
                .flatMap(Optional::stream)
                .forEach(member -> {
                    GlowManager.disable(member);
                    Lang.send(member, "glow.disabled", placeholders);
                });

        GlowUpdater.forceUpdateAll(clan);
    }
}
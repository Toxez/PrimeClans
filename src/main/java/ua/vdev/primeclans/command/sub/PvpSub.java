package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class PvpSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.hasPerm(player.getUniqueId(), ClanPerm.TOGGLE_PVP)) {
                Lang.send(player, "perm.no-perm");
                return;
            }

            boolean newState = !clan.pvpEnabled();
            clanManager.setPvpEnabled(clan.name(), newState);

            Map<String, String> placeholders = Map.of("clan", clan.name());
            String statusKey = newState ? "pvp.enabled" : "pvp.disabled";

            clan.members().stream()
                    .map(PlayerFind::uuid)
                    .flatMap(Optional::stream)
                    .forEach(member -> Lang.send(member, statusKey, placeholders));

        }, () -> Lang.send(player, "pvp.no-clan"));
    }

    @Override
    public String getName() { return "pvp"; }
}
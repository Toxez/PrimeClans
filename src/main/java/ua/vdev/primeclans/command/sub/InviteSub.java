package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Map;

@RequiredArgsConstructor
public class InviteSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.hasPerm(player.getUniqueId(), ClanPerm.INVITE_MEMBERS)) {
                Lang.send(player, "perm.no-perm");
                return;
            }

            if (args.length < 2) {
                Lang.send(player, "invite.usage");
                return;
            }

            int current = clan.members().size();
            if (current >= clan.getMaxMembers()) {
                Lang.send(player, "invite.full-owner", Map.of(
                        "clan", clan.name(),
                        "current", String.valueOf(current),
                        "max", String.valueOf(clan.getMaxMembers())
                ));
                return;
            }

            PlayerFind.name(args[1]).ifPresentOrElse(target -> {
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    Lang.send(player, "invite.invite-self");
                    return;
                }
                if (clanManager.getPlayerClan(target.getUniqueId()).isPresent()) {
                    Lang.send(player, "invite.already-in-clan");
                    return;
                }

                clanManager.createInvite(target.getUniqueId(), clan.name());
                int time = PrimeClans.getInstance().getConfig().getInt("settings.invite-timeout", 60);
                Lang.send(player, "invite.sent", Map.of("player", target.getName()));
                Lang.send(target, "invite.received", Map.of(
                        "clan", clan.name(),
                        "time", String.valueOf(time)
                ));
            }, () -> Lang.send(player, "invite.player-not-found"));
        }, () -> Lang.send(player, "help.no-clan"));
    }

    @Override
    public String getName() { return "invite"; }
}
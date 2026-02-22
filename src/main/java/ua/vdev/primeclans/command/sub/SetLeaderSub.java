package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SetLeaderSub implements SubCommand {
    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.isOwner(player.getUniqueId())) {
                Lang.send(player, "setleader.not-owner");
                return;
            }

            if (args.length < 2) {
                Lang.send(player, "setleader.usage");
                return;
            }

            String targetName = args[1];

            Optional<UUID> targetUUID = clan.members().stream()
                    .map(Bukkit::getOfflinePlayer)
                    .filter(op -> op.getName() != null && op.getName().equalsIgnoreCase(targetName))
                    .map(OfflinePlayer::getUniqueId)
                    .findFirst();

            targetUUID.ifPresentOrElse(uuid -> {
                if (uuid.equals(player.getUniqueId())) {
                    Lang.send(player, "setleader.self");
                    return;
                }

                clanManager.setOwner(clan.name(), uuid);
                Lang.send(player, "setleader.success", Map.of("player", targetName));

                PlayerFind.uuid(uuid).ifPresent(target ->
                        Lang.send(target, "setleader.new-leader-notify", Map.of("player", player.getName()))
                );

            }, () -> Lang.send(player, "setleader.not-in-clan"));

        }, () -> Lang.send(player, "help.no-clan"));
    }

    @Override
    public String getName() {return "setleader";}
}
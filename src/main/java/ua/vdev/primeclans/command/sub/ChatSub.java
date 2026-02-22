package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;
import ua.vdev.vlibapi.player.PlayerMsg;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public class ChatSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.hasPerm(player.getUniqueId(), ClanPerm.CLAN_CHAT)) {
                Lang.send(player, "perm.no-perm");
                return;
            }

            if (args.length < 2) {
                Lang.send(player, "clan-chat.usage");
                return;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            if (message.isEmpty()) {
                Lang.send(player, "clan-chat.empty-message");
                return;
            }

            FileConfiguration config = PrimeClans.getInstance().getConfig();
            String format = config.getString("settings.clan-chat-format");

            String ownerDisplay  = config.getString("settings.role.owner",  "<#FACC15>Лидер");
            String memberDisplay = config.getString("settings.role.member", "<#62DE3F>Участник");
            String roleDisplay   = clan.isOwner(player.getUniqueId()) ? ownerDisplay : memberDisplay;

            String formatted = format
                    .replace("{role}", roleDisplay)
                    .replace("{player}", player.getName())
                    .replace("{message}", message);

            clan.members().stream()
                    .map(PlayerFind::uuid)
                    .flatMap(Optional::stream)
                    .forEach(member -> PlayerMsg.send(member, formatted));

        }, () -> Lang.send(player, "clan-chat.no-clan"));
    }

    @Override
    public String getName() { return "chat"; }
}
package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.storage.StorageManager;
import ua.vdev.primeclans.util.Lang;

@RequiredArgsConstructor
public class StorageSub implements SubCommand {

    private final ClanManager clanManager;
    private final StorageManager storageManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(clan -> {
            if (!clan.hasPerm(player.getUniqueId(), ClanPerm.valueOf("STORAGE_ACCESS"))) {
                Lang.send(player, "perm.no-perm");
                return;
            }
            storageManager.openStorage(player, clan);
        }, () -> Lang.send(player, "help.no-clan"));
    }

    @Override
    public String getName() {
        return "storage";
    }
}
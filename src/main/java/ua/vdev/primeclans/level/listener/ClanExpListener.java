package ua.vdev.primeclans.level.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import ua.vdev.primeclans.level.ClanLevelService;
import ua.vdev.primeclans.manager.ClanManager;

public class ClanExpListener implements Listener {

    private final ClanManager clanManager;
    private final ClanLevelService levelService;

    public ClanExpListener(ClanManager clanManager, ClanLevelService levelService) {
        this.clanManager = clanManager;
        this.levelService = levelService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player p = e.getPlayer();
        long exp = levelService.expFor("block-break");
        if (exp > 0) {
            clanManager.addExp(p.getUniqueId(), exp);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;
        if (e.getEntity() instanceof Player victim) {
            if (clanManager.isSameClan(killer.getUniqueId(), victim.getUniqueId())) {
                return;
            }
        }

        String source = e.getEntity() instanceof Player ? "player-kill" : "mob-kill";
        long exp = levelService.expFor(source);
        if (exp > 0) {
            clanManager.addExp(killer.getUniqueId(), exp);
        }
    }
}
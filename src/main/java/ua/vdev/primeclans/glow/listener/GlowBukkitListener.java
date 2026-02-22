package ua.vdev.primeclans.glow.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.glow.manager.GlowManager;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.util.Optional;

public class GlowBukkitListener implements Listener {
    private final ClanManager clanManager;

    public GlowBukkitListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Optional.ofNullable(e.getPlayer())
                .flatMap(p -> clanManager.getPlayerClan(p.getUniqueId()))
                .ifPresent(ignored -> GlowManager.enable(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Optional.ofNullable(e.getPlayer())
                .ifPresent(GlowManager::remove);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Optional.ofNullable(e.getPlayer())
                .filter(GlowManager::isEnabled)
                .ifPresent(p -> Task.later(4L, p::updateInventory));
    }
}

package ua.vdev.primeclans.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.model.Clan;

import java.util.Optional;

@RequiredArgsConstructor
public class PvpListener implements Listener {
    private final ClanManager clanManager;

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        getAttacker(event).ifPresent(attacker -> {
            if (attacker.equals(victim)) return;
            boolean sameClan = clanManager.isSameClan(attacker.getUniqueId(), victim.getUniqueId());
            if (sameClan) {
                Optional<Clan> clanOpt = clanManager.getPlayerClan(attacker.getUniqueId());
                if (clanOpt.isPresent() && !clanOpt.get().pvpEnabled()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private Optional<Player> getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return Optional.of(player);
        }

        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return Optional.of(player);
            }
        }

        return Optional.empty();
    }
}
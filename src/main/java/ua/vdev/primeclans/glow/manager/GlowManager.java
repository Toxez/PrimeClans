package ua.vdev.primeclans.glow.manager;

import org.bukkit.entity.Player;
import ua.vdev.vlibapi.player.PlayerFind;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GlowManager {
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

    private GlowManager() {}

    public static void enable(Player player) {
        Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .ifPresent(ENABLED::add);
    }

    public static void disable(Player player) {
        Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .ifPresent(ENABLED::remove);
    }

    public static boolean isEnabled(Player player) {
        return Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .map(ENABLED::contains)
                .orElse(false);
    }

    public static void remove(Player player) {
        Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .ifPresent(ENABLED::remove);
    }

    public static void clear() {
        ENABLED.clear();
    }

    public static Set<UUID> getEnabledPlayers() {
        return Set.copyOf(ENABLED);
    }

    public static int getEnabledCount() {
        return ENABLED.size();
    }

    public static Set<Player> getEnabledOnlinePlayers() {
        return PlayerFind.all()
                .matching(GlowManager::isEnabled)
                .asSet();
    }
}

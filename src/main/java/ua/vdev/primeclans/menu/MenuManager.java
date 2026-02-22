package ua.vdev.primeclans.menu;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {
    private static final Map<UUID, Menu> activeMenus = new ConcurrentHashMap<>();

    public static void openMenu(Player player, Menu menu) {
        Optional.ofNullable(player)
                .ifPresent(p -> {
                    activeMenus.put(p.getUniqueId(), menu);
                    menu.open(p);
                });
    }

    public static Optional<Menu> getActiveMenu(Player player) {
        return Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .flatMap(uuid -> Optional.ofNullable(activeMenus.get(uuid)));
    }

    public static void closeMenu(Player player) {
        Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .ifPresent(activeMenus::remove);
    }

    public static boolean hasActiveMenu(Player player) {
        return Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .map(activeMenus::containsKey)
                .orElse(false);
    }

    public static void closeMenuIfMatching(Player player, String menuId) {
        Optional.ofNullable(player)
                .map(Player::getUniqueId)
                .flatMap(uuid -> Optional.ofNullable(activeMenus.get(uuid))
                        .filter(menu -> menu.getId().equals(menuId)))
                .ifPresent(menu -> activeMenus.remove(player.getUniqueId()));
    }
}
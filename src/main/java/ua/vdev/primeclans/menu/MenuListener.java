package ua.vdev.primeclans.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.Optional;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
            if (event.getClick() == ClickType.SWAP_OFFHAND || event.getClick() == ClickType.NUMBER_KEY) {
                return;
            }

            if (event.getClickedInventory() != null &&
                    event.getClickedInventory().equals(event.getView().getTopInventory())) {
                MenuManager.getActiveMenu(player).ifPresent(menu -> menu.handleClick(player, event));
            }
            return;
        }

        MenuManager.getActiveMenu(player).ifPresent(menu -> {
            event.setCancelled(true);
            if (event.getClick() == ClickType.SWAP_OFFHAND || event.getClick() == ClickType.NUMBER_KEY) {
                return;
            }
            if (event.getClickedInventory() != null &&
                    event.getClickedInventory().equals(event.getView().getTopInventory())) {
                menu.handleClick(player, event);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
            return;
        }
        MenuManager.getActiveMenu(player).ifPresent(menu -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
        Optional.ofNullable(event.getInventory().getHolder())
                .filter(MenuHolder.class::isInstance)
                .map(MenuHolder.class::cast)
                .map(MenuHolder::getMenuId)
                .ifPresent(menuId -> MenuManager.closeMenuIfMatching(player, menuId));
    }
}
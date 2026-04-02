package ua.vdev.primeclans.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Optional;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuHolder menuHolder)) return;
        MenuManager.getActiveMenu(player).ifPresent(activeMenu -> {
            if (!menuHolder.getMenuId().equals(activeMenu.getId())) {
                return;
            }

            event.setCancelled(true);
            if (event.getClick() == ClickType.SWAP_OFFHAND || event.getClick() == ClickType.NUMBER_KEY) {
                return;
            }

            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                activeMenu.handleClick(player, event);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof MenuHolder menuHolder) {
            MenuManager.getActiveMenu(player).ifPresent(activeMenu -> {
                if (menuHolder.getMenuId().equals(activeMenu.getId())) {
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Optional.ofNullable(event.getInventory().getHolder())
                .filter(MenuHolder.class::isInstance)
                .map(MenuHolder.class::cast)
                .map(MenuHolder::getMenuId)
                .ifPresent(menuId -> MenuManager.closeMenuIfMatching(player, menuId));
    }
}
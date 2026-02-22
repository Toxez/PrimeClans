package ua.vdev.primeclans.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {
    private final String menuId;
    public MenuHolder(String menuId) {this.menuId = menuId;}
    public String getMenuId() {return menuId;}
    @Override public Inventory getInventory() {return null;}
}
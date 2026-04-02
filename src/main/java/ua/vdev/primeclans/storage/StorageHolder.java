package ua.vdev.primeclans.storage;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import ua.vdev.primeclans.menu.action.MenuAction;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StorageHolder implements InventoryHolder {
    private final String clanName;
    private final Set<Integer> allowedSlots;
    private final Map<Integer, List<MenuAction>> leftActions;
    private final Map<Integer, List<MenuAction>> rightActions;

    public StorageHolder(String clanName, Set<Integer> allowedSlots, Map<Integer, List<MenuAction>> leftActions, Map<Integer, List<MenuAction>> rightActions) {
        this.clanName = clanName;
        this.allowedSlots = allowedSlots;
        this.leftActions = leftActions;
        this.rightActions = rightActions;
    }

    public String getClanName() { return clanName; }
    public Set<Integer> getAllowedSlots() { return allowedSlots; }
    public Map<Integer, List<MenuAction>> getLeftActions() { return leftActions; }
    public Map<Integer, List<MenuAction>> getRightActions() { return rightActions; }
    @Override public @NotNull Inventory getInventory() { return null; }
}
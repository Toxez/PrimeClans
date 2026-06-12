package ua.vdev.primeclans.storage.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.storage.StorageHolder;
import ua.vdev.primeclans.storage.StorageManager;

public class StorageListener implements Listener {

    private final StorageManager storageManager;

    public StorageListener(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (
            !(e.getInventory().getHolder() instanceof StorageHolder holder)
        ) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;
        int rawSlot = e.getRawSlot();
        if (rawSlot < 0) return;
        boolean isTopInventory =
            rawSlot < e.getView().getTopInventory().getSize();

        if (isTopInventory) {
            if (holder.getAllowedSlots().contains(rawSlot)) return;
            e.setCancelled(true);
            if (
                e.getClick() == ClickType.SWAP_OFFHAND ||
                e.getClick() == ClickType.NUMBER_KEY
            ) {
                return;
            }

            List<MenuAction> actions = e.getClick().isLeftClick()
                ? holder.getLeftActions().get(rawSlot)
                : holder.getRightActions().get(rawSlot);

            if (actions != null) {
                actions.forEach(action -> action.execute(player));
            }
        } else {
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                handleShiftClick(e, holder);
            }
            if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (
            !(e.getInventory().getHolder() instanceof StorageHolder holder)
        ) return;
        int topSize = e.getView().getTopInventory().getSize();
        for (int slot : e.getRawSlots()) {
            if (slot < topSize && !holder.getAllowedSlots().contains(slot)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof StorageHolder holder)) return;
        if (inv.getViewers().size() <= 1) {
            storageManager.saveAndUnload(holder.getClanName(), inv);
        }
    }

    private void handleShiftClick(InventoryClickEvent e, StorageHolder holder) {
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType().isAir()) return;
        e.setCancelled(true);

        Inventory topInv = e.getView().getTopInventory();
        int amountLeft = current.getAmount();
        List<Integer> sortedSlots = new ArrayList<>(holder.getAllowedSlots());
        Collections.sort(sortedSlots);
        for (int slot : sortedSlots) {
            ItemStack inSlot = topInv.getItem(slot);
            if (
                inSlot != null &&
                inSlot.isSimilar(current) &&
                inSlot.getAmount() < inSlot.getMaxStackSize()
            ) {
                int space = inSlot.getMaxStackSize() - inSlot.getAmount();
                int toAdd = Math.min(space, amountLeft);

                inSlot.setAmount(inSlot.getAmount() + toAdd);
                amountLeft -= toAdd;

                if (amountLeft <= 0) break;
            }
        }

        if (amountLeft > 0) {
            for (int slot : sortedSlots) {
                ItemStack inSlot = topInv.getItem(slot);
                if (inSlot == null || inSlot.getType().isAir()) {
                    ItemStack clone = current.clone();
                    clone.setAmount(amountLeft);
                    topInv.setItem(slot, clone);
                    amountLeft = 0;
                    break;
                }
            }
        }

        if (amountLeft <= 0) {
            e.setCurrentItem(null);
        } else {
            current.setAmount(amountLeft);
        }
    }
}

package ua.vdev.primeclans.menu.helper;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.vlibapi.item.ItemBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  - {@link #getId()}
 *  - {@link #createInventory(ConfigurationSection, Map)}
 *  - {@link #loadStaticItems(Inventory, ConfigurationSection, Map)}
 *  - {@link #loadPageItems(Inventory, ConfigurationSection, List, int)}
 *  - {@link #buildPlaceholders(int, int)}
 *  - {@link #totalItems()} — сколько всего элементов нужно отобразить
 *  - {@link #handlePlayerSlotClick(Player, int)} — это что происходит при клике на слот с элементом
 */
public abstract class PaginatedMenu implements Menu {
    protected final int page;
    protected final Map<Integer, List<MenuAction>> leftActions  = new HashMap<>();
    protected final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    protected PaginatedMenu(int page) {
        this.page = Math.max(0, page);
    }

    @Override
    public final void open(Player player) {
        Optional.ofNullable(getMenuSection()).ifPresent(menuSection -> {
            List<Integer> playerSlots = parsePlayerSlots(menuSection);
            int totalPages = calcTotalPages(playerSlots.size());
            int safePage = Math.min(page, Math.max(0, totalPages - 1));

            Map<String, String> placeholders = buildPlaceholders(safePage, totalPages);
            Inventory inventory = createInventory(menuSection, placeholders);

            leftActions.clear();
            rightActions.clear();

            loadStaticItems(inventory, menuSection, placeholders);
            loadPageItems(inventory, menuSection, playerSlots, safePage);
            loadPaginationButtons(inventory, menuSection, safePage, totalPages, placeholders);

            player.openInventory(inventory);
        });
    }

    @Override
    public final void handleClick(Player player, InventoryClickEvent event) {
        Optional.ofNullable(event.getClickedInventory())
                .filter(inv -> inv.equals(event.getView().getTopInventory()))
                .ifPresent(inv -> {
                    event.setCancelled(true);
                    int slot = event.getRawSlot();
                    Map<Integer, List<MenuAction>> actionsMap = event.getClick().isLeftClick()
                            ? leftActions
                            : event.getClick().isRightClick()
                            ? rightActions
                            : null;

                    if (actionsMap != null && actionsMap.containsKey(slot)) {
                        actionsMap.get(slot).forEach(a -> a.execute(player));
                        return;
                    }

                    handlePlayerSlotClick(player, slot);
                });
    }

    private void loadPaginationButtons(Inventory inventory, ConfigurationSection menuSection, int currentPage, int totalPages, Map<String, String> placeholders) {
        if (totalPages <= 1) return;

        int prevSlot = menuSection.getInt("pagination.prev-slot", 48);
        int nextSlot = menuSection.getInt("pagination.next-slot", 50);

        if (currentPage > 0) {
            buildPaginationItem(menuSection, "pagination.prev-item", placeholders)
                    .ifPresent(item -> {
                        inventory.setItem(prevSlot, item);
                        List<MenuAction> action = List.of(p -> MenuManager.openMenu(p, prevPage()));
                        leftActions.put(prevSlot,  action);
                        rightActions.put(prevSlot, action);
                    });
        }

        if (currentPage < totalPages - 1) {
            buildPaginationItem(menuSection, "pagination.next-item", placeholders)
                    .ifPresent(item -> {
                        inventory.setItem(nextSlot, item);
                        List<MenuAction> action = List.of(p -> MenuManager.openMenu(p, nextPage()));
                        leftActions.put(nextSlot,  action);
                        rightActions.put(nextSlot, action);
                    });
        }
    }

    private Optional<org.bukkit.inventory.ItemStack> buildPaginationItem(ConfigurationSection menuSection, String path, Map<String, String> placeholders) {
        return Optional.ofNullable(menuSection.getConfigurationSection(path))
                .map(sec -> sec.getValues(false))
                .map(map -> ItemBuilder.fromMap(map, placeholders));
    }

    protected List<Integer> parsePlayerSlots(ConfigurationSection menuSection) {
        return Optional.ofNullable(menuSection.getList("player-slots"))
                .stream()
                .flatMap(List::stream)
                .filter(obj -> obj instanceof Number)
                .map(obj -> ((Number) obj).intValue())
                .collect(Collectors.toList());
    }

    protected int calcTotalPages(int perPage) {
        if (perPage <= 0) return 1;
        return (int) Math.ceil((double) totalItems() / perPage);
    }

    protected abstract ConfigurationSection getMenuSection();
    protected abstract Inventory createInventory(ConfigurationSection menuSection, Map<String, String> placeholders);
    protected abstract void loadStaticItems(Inventory inventory, ConfigurationSection menuSection, Map<String, String> placeholders);
    protected abstract void loadPageItems(Inventory inventory, ConfigurationSection menuSection, List<Integer> playerSlots, int currentPage);
    protected abstract Map<String, String> buildPlaceholders(int currentPage, int totalPages);
    protected abstract int totalItems();
    protected abstract void handlePlayerSlotClick(Player player, int slot);
    protected abstract Menu prevPage();
    protected abstract Menu nextPage();
}
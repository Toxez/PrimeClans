package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.MenuType;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.config.MenuConfig;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.vlibapi.util.TextColor;

import java.util.*;

public class ConfirmCreate implements Menu {

    private final String clanName;
    private final MenuConfig menuConfig;
    private final Map<String, String> placeholders;
    private final Map<Integer, List<MenuAction>> leftActions = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    public ConfirmCreate(String clanName) {
        this.clanName = clanName;
        this.menuConfig = new MenuConfig(MenuType.CONFIRM_CREATE);
        double cost = PrimeClans.getInstance().getConfig().getDouble("settings.create-cost", 0.0);
        String formattedCost = PrimeClans.getInstance().getEconomyManager().format(cost);
        this.placeholders = Map.of("clan", clanName, "cost", formattedCost);
    }

    @Override
    public void open(Player player) {
        YamlConfiguration config = menuConfig.get();
        Optional.ofNullable(config.getConfigurationSection("menu"))
                .ifPresent(menuSection -> {
                    Inventory inventory = createInventory(menuSection);
                    loadItems(inventory, menuSection);
                    player.openInventory(inventory);
                });
    }

    private Inventory createInventory(ConfigurationSection menuSection) {
        String title = menuSection.getString("title", "Подтверждение создания");
        int size = menuSection.getInt("size", 27);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders)
        );
    }

    private void loadItems(Inventory inventory, ConfigurationSection menuSection) {
        Map<String, Object> actionContext = Map.of(
                "clan_name", clanName,
                "placeholders", placeholders
        );

        MenuHelper.loadMenuItems(
                inventory,
                menuSection,
                placeholders,
                actionContext,
                leftActions,
                rightActions
        );
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        if (!isTopInventoryClick(event)) return;
        int slot = event.getRawSlot();
        getActionsForClick(event, slot)
                .ifPresent(actions -> {
                    event.setCancelled(true);
                    actions.forEach(action -> action.execute(player));
                });
    }

    private boolean isTopInventoryClick(InventoryClickEvent event) {
        return Optional.ofNullable(event.getClickedInventory())
                .map(inv -> inv.equals(event.getView().getTopInventory()))
                .orElse(false);
    }

    private Optional<List<MenuAction>> getActionsForClick(InventoryClickEvent event, int slot) {
        Map<Integer, List<MenuAction>> actionsMap = event.getClick().isLeftClick()
                ? leftActions
                : event.getClick().isRightClick()
                ? rightActions
                : null;

        return Optional.ofNullable(actionsMap)
                .map(map -> map.get(slot));
    }

    @Override
    public String getId() {return "confirm-create";}
}
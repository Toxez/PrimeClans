package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.MenuType;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.config.MenuConfig;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.util.TextColor;

import java.util.*;

public class GlowMenu implements Menu {

    private final Clan clan;
    private final MenuConfig menuConfig;
    private final Map<String, String> placeholders;
    private final Map<Integer, List<MenuAction>> leftActions = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    public GlowMenu(Clan clan) {
        this.clan = clan;
        this.menuConfig = new MenuConfig(MenuType.GLOW_MENU);
        this.placeholders = buildPlaceholders();
    }

    private Map<String, String> buildPlaceholders() {
        String currentColor = Optional.ofNullable(clan.glowColor())
                .map(GlowColor::toHex)
                .orElse("<gray>Не установлен");

        return Map.of(
                "clan_name", clan.name(),
                "glow_color", currentColor
        );
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
        String title = menuSection.getString("title", "&0Свечение клана");
        int size = menuSection.getInt("size", 27);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders)
        );
    }

    private void loadItems(Inventory inventory, ConfigurationSection menuSection) {
        Map<String, Object> actionContext = Map.of(
                "clan_name", clan.name(),
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
    public String getId() {return "glow";}
}
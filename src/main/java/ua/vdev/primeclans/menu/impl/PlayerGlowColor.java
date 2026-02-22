package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.MenuType;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.config.MenuConfig;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.util.TextColor;
import java.util.*;

public class PlayerGlowColor implements Menu {

    private final Clan clan;
    private final UUID targetUuid;
    private final MenuConfig menuConfig;
    private final Map<String, String> placeholders;
    private final Map<Integer, List<MenuAction>> leftActions  = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    public PlayerGlowColor(Clan clan, UUID targetUuid) {
        this.clan = clan;
        this.targetUuid = targetUuid;
        this.menuConfig = new MenuConfig(MenuType.PLAYER_GLOW_COLOR);
        this.placeholders = buildPlaceholders();
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
        String title = Optional.ofNullable(menuSection.getString("title"))
                .orElse("Цвет свечения игрока");
        int size = menuSection.getInt("size", 54);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders)
        );
    }

    private void loadItems(Inventory inventory, ConfigurationSection menuSection) {
        Map<String, Object> actionContext = Map.of(
                "clan_name", clan.name(),
                "target_uuid", targetUuid.toString(),
                "placeholders", placeholders
        );
        MenuHelper.loadMenuItems(inventory, menuSection, placeholders, actionContext, leftActions, rightActions);
    }

    private Map<String, String> buildPlaceholders() {
        String playerName = Optional.of(targetUuid)
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .orElse("Unknown");

        String currentColor = Optional.ofNullable(clan.memberColors().get(targetUuid))
                .map(c -> c.toHex() + " <gray>[личный]")
                .orElseGet(() -> Optional.ofNullable(clan.glowColor())
                        .map(c -> c.toHex() + " <gray>[клановый]")
                        .orElse("<gray>Не установлен"));

        return Map.of(
                "player_name", playerName,
                "player_uuid", targetUuid.toString(),
                "clan_name", clan.name(),
                "current_color", currentColor
        );
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
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
                    Optional.ofNullable(actionsMap)
                            .map(map -> map.get(slot))
                            .ifPresent(actions -> actions.forEach(a -> a.execute(player)));
                });
    }

    @Override
    public String getId() {return "player-glow-color";}
}
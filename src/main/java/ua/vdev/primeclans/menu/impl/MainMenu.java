package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.level.ClanLevelService;
import ua.vdev.primeclans.level.model.ClanLevel;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.MenuType;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.config.MenuConfig;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.util.TextColor;

import java.util.*;

public class MainMenu implements Menu {

    private final Clan clan;
    private final MenuConfig menuConfig;
    private final Map<String, String> placeholders;
    private final Map<Integer, List<MenuAction>> leftActions = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    public MainMenu(Clan clan) {
        this.clan = clan;
        this.menuConfig = new MenuConfig(MenuType.MAIN_MENU);
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
                .orElse("Меню клана");
        int size = menuSection.getInt("size", 54);
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

    private Map<String, String> buildPlaceholders() {
        ClanLevelService levelService = PrimeClans.getInstance().getLevelService();
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();
        String ownerName = Optional.of(clan.owner())
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .orElse("хз");

        long currentExp = clan.exp();
        int currentLevel = clan.level();
        long requiredExpForNextLevel = Optional.ofNullable(
                        levelService.levels().get(currentLevel + 1))
                .map(ClanLevel::requiredExp)
                .orElse(0L);
        long expToNextLevel = requiredExpForNextLevel > 0
                ? Math.max(0, requiredExpForNextLevel - currentExp)
                : 0;
        long membersOnline = clan.members().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .count();
        String formattedBalance = economy.format(clan.balance());
        return Map.ofEntries(
                Map.entry("clan_name", clan.name()),
                Map.entry("clan_owner", ownerName),
                Map.entry("clan_level", String.valueOf(currentLevel)),
                Map.entry("clan_exp", String.valueOf(currentExp)),
                Map.entry("clan_exp_to_next", String.valueOf(expToNextLevel)),
                Map.entry("clan_required_exp", String.valueOf(requiredExpForNextLevel)),
                Map.entry("clan_members_current", String.valueOf(clan.members().size())),
                Map.entry("clan_members_max", String.valueOf(clan.getMaxMembers())),
                Map.entry("clan_members_info", clan.members().size() + "/" + clan.getMaxMembers()),
                Map.entry("members_online", String.valueOf(membersOnline)),
                Map.entry("clan_balance", formattedBalance)
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
    public String getId() {return "main-menu";}
}
package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.vlibapi.util.TextColor;

import java.util.*;

public class PlayerPerm implements Menu {

    private final Clan clan;
    private final UUID targetUuid;
    private final MenuConfig menuConfig;
    private final Map<Integer, List<MenuAction>> leftActions  = new HashMap<>();
    private final Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

    private static final Map<ClanPerm, String> PERM_KEYS = Map.of(
            ClanPerm.KICK_MEMBERS,"kick",
            ClanPerm.INVITE_MEMBERS,"invite",
            ClanPerm.CLAN_CHAT,"chat",
            ClanPerm.INVEST_BALANCE,"invest",
            ClanPerm.WITHDRAW_BALANCE,"withdraw",
            ClanPerm.TOGGLE_PVP,"pvp",
            ClanPerm.MANAGE_GLOW,"glow",
            ClanPerm.MANAGE_MEMBER_GLOW,"member_glow"
    );

    public PlayerPerm(Clan clan, UUID targetUuid) {
        this.clan = clan;
        this.targetUuid = targetUuid;
        this.menuConfig = new MenuConfig(MenuType.PLAYER_PERM);
    }

    @Override
    public void open(Player player) {
        YamlConfiguration config = menuConfig.get();
        Optional.ofNullable(config.getConfigurationSection("menu")).ifPresent(menuSection -> {
            Map<String, String> placeholders = buildPlaceholders(menuSection);
            Inventory inventory = createInventory(menuSection, placeholders);
            leftActions.clear();
            rightActions.clear();
            Map<String, Object> actionContext = Map.of(
                    "clan_name", clan.name(),
                    "target_uuid", targetUuid.toString(),
                    "placeholders", placeholders
            );

            MenuHelper.loadMenuItems(inventory, menuSection, placeholders, actionContext, leftActions, rightActions);
            player.openInventory(inventory);
        });
    }

    private Inventory createInventory(ConfigurationSection menuSection, Map<String, String> placeholders) {
        String title = Optional.ofNullable(menuSection.getString("title")).orElse("Права {player_name}");
        int size = menuSection.getInt("size", 54);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders));
    }

    private Map<String, String> buildPlaceholders(ConfigurationSection menuSection) {
        String rawEnabledMat = menuSection.getString("perm-states.enabled.material", "GREEN_STAINED_GLASS_PANE");
        String rawDisabledMat = menuSection.getString("perm-states.disabled.material", "RED_STAINED_GLASS_PANE");
        String rawEnabledStatus = menuSection.getString("perm-states.enabled.status", "<#52C25A>✔ Разрешено");
        String rawDisabledStatus= menuSection.getString("perm-states.disabled.status", "<#C25252>✘ Запрещено");

        final String matOn = isValidMaterial(rawEnabledMat) ? rawEnabledMat : "GREEN_STAINED_GLASS_PANE";
        final String matOff = isValidMaterial(rawDisabledMat) ? rawDisabledMat : "RED_STAINED_GLASS_PANE";
        final String statusOn = rawEnabledStatus;
        final String statusOff = rawDisabledStatus;

        Set<ClanPerm> currentPerms = Optional.ofNullable(clan.memberPerms().get(targetUuid))
                .orElse(Collections.emptySet());
        String playerName = Optional.of(targetUuid)
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .orElse("Unknown");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player_name", playerName);
        placeholders.put("player_uuid", targetUuid.toString());
        placeholders.put("clan_name",   clan.name());

        PERM_KEYS.forEach((perm, key) -> {
            boolean has = currentPerms.contains(perm);
            placeholders.put("perm_" + key + "_material", has ? matOn : matOff);
            placeholders.put("perm_" + key + "_status", has ? statusOn : statusOff);
        });

        return Collections.unmodifiableMap(placeholders);
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
                            .map(m -> m.get(slot))
                            .ifPresent(actions -> actions.forEach(a -> a.execute(player)));
                });
    }

    private static boolean isValidMaterial(String name) {
        try {
            Material.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String getId() { return "player-perm"; }
}
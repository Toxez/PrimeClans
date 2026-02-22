package ua.vdev.primeclans.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.menu.MenuHolder;
import ua.vdev.primeclans.menu.MenuManager;
import ua.vdev.primeclans.menu.MenuType;
import ua.vdev.primeclans.menu.config.MenuConfig;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.menu.helper.PaginatedMenu;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.vlibapi.item.ItemBuilder;
import ua.vdev.vlibapi.util.TextColor;

import java.util.*;

public class PlayerGlowList extends PaginatedMenu {

    private final Clan clan;
    private final MenuConfig menuConfig;
    private final Map<Integer, UUID> slotToPlayer = new HashMap<>();

    public PlayerGlowList(Clan clan) {
        this(clan, 0);
    }

    public PlayerGlowList(Clan clan, int page) {
        super(page);
        this.clan = clan;
        this.menuConfig = new MenuConfig(MenuType.PLAYER_GLOW_LIST);
    }

    @Override
    protected ConfigurationSection getMenuSection() {
        return menuConfig.get().getConfigurationSection("menu");
    }

    @Override
    protected Inventory createInventory(ConfigurationSection menuSection, Map<String, String> placeholders) {
        String title = Optional.ofNullable(menuSection.getString("title")).orElse("Свечение участников");
        int size = menuSection.getInt("size", 54);
        return Bukkit.createInventory(new MenuHolder(getId()), size, TextColor.parse(title, placeholders));
    }

    @Override
    protected void loadStaticItems(Inventory inventory, ConfigurationSection menuSection, Map<String, String> placeholders) {
        Map<String, Object> actionContext = Map.of(
                "clan_name", clan.name(),
                "placeholders", placeholders
        );
        MenuHelper.loadMenuItems(inventory, menuSection, placeholders, actionContext, leftActions, rightActions);
    }

    @Override
    protected void loadPageItems(Inventory inventory, ConfigurationSection menuSection, List<Integer> playerSlots, int currentPage) {
        List<UUID> members = new ArrayList<>(clan.members());
        int perPage = playerSlots.size();
        int fromIndex = currentPage * perPage;
        int toIndex = Math.min(fromIndex + perPage, members.size());
        if (fromIndex >= members.size()) return;
        FileConfiguration config = PrimeClans.getInstance().getConfig();
        List<UUID> pageMembers = members.subList(fromIndex, toIndex);
        slotToPlayer.clear();
        for (int i = 0; i < pageMembers.size(); i++) {
            UUID memberUuid = pageMembers.get(i);
            int  slot = playerSlots.get(i);
            createPlayerItem(memberUuid, config, menuSection).ifPresent(item -> {
                inventory.setItem(slot, item);
                slotToPlayer.put(slot, memberUuid);
            });
        }
    }

    private Optional<org.bukkit.inventory.ItemStack> createPlayerItem(UUID uuid, FileConfiguration config, ConfigurationSection menuSection) {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid))
                .flatMap(op -> Optional.ofNullable(menuSection.getConfigurationSection("player-item"))
                        .map(sec -> sec.getValues(false))
                        .map(map -> ItemBuilder.fromMap(map, buildPlayerPlaceholders(op, config))));
    }

    private Map<String, String> buildPlayerPlaceholders(OfflinePlayer player, FileConfiguration config) {
        String status = player.isOnline()
                ? config.getString("settings.player-status.online",  "<#52C25A>Онлайн")
                : config.getString("settings.player-status.offline", "<#C25252>Оффлайн");
        String role = clan.isOwner(player.getUniqueId())
                ? config.getString("settings.role.owner",  "<#FCD05C>Лидер")
                : config.getString("settings.role.member", "<#52C25A>Участник");
        String glowColor = Optional.ofNullable(clan.memberColors().get(player.getUniqueId()))
                .map(GlowColor::toHex)
                .orElse("<gray>Общий");

        return Map.of(
                "player_name", Optional.ofNullable(player.getName()).orElse("хз"),
                "player_status", status,
                "player_role", role,
                "player_uuid", player.getUniqueId().toString(),
                "member_glow_color", glowColor
        );
    }

    @Override
    protected Map<String, String> buildPlaceholders(int currentPage, int totalPages) {
        return Map.of(
                "clan_name", clan.name(),
                "members_count", String.valueOf(clan.members().size()),
                "members_max", String.valueOf(clan.getMaxMembers()),
                "page", String.valueOf(currentPage + 1),
                "total_pages", String.valueOf(totalPages)
        );
    }

    @Override
    protected int totalItems() { return clan.members().size(); }

    @Override
    protected void handlePlayerSlotClick(Player player, int slot) {
        Optional.ofNullable(slotToPlayer.get(slot))
                .ifPresent(targetUuid ->
                        MenuManager.openMenu(player, new PlayerGlowColor(clan, targetUuid)));
    }

    @Override protected Menu prevPage() { return new PlayerGlowList(clan, page - 1); }
    @Override protected Menu nextPage() { return new PlayerGlowList(clan, page + 1); }
    @Override public String getId() { return "player-glow-list"; }
}
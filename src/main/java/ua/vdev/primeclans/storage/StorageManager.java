package ua.vdev.primeclans.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.database.Db;
import ua.vdev.primeclans.level.model.ClanLevel;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.menu.helper.MenuHelper;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.storage.util.ItemSerializer;
import ua.vdev.vlibapi.item.ItemBuilder;
import ua.vdev.vlibapi.util.TextColor;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private final Map<String, Inventory> cache = new ConcurrentHashMap<>();
    private final Set<String> loading = ConcurrentHashMap.newKeySet();
    private final Db db;
    private YamlConfiguration config;

    public StorageManager(Db db) {
        this.db = db;
        reloadConfig();
    }

    public void reloadConfig() {
        File file = new File(PrimeClans.getInstance().getDataFolder(), "menu/storage.yml");
        if (!file.exists()) {
            PrimeClans.getInstance().saveResource("menu/storage.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void openStorage(Player player, Clan clan) {
        String lowerName = clan.name().toLowerCase();

        if (cache.containsKey(lowerName)) {
            player.openInventory(cache.get(lowerName));
            return;
        }

        if (loading.contains(lowerName)) return;
        loading.add(lowerName);

        db.loadStorage(lowerName).thenAccept(base64 -> {
            Task.sync(() -> {
                loading.remove(lowerName);
                if (cache.containsKey(lowerName)) {
                    player.openInventory(cache.get(lowerName));
                    return;
                }

                Inventory inv = createGui(clan, base64);
                cache.put(lowerName, inv);
                player.openInventory(inv);
            });
        });
    }

    private Inventory createGui(Clan clan, String base64) {
        ConfigurationSection menuSection = config.getConfigurationSection("menu");
        if (menuSection == null) menuSection = config.createSection("menu");
        String title = menuSection.getString("title", "<black>Хранилище");
        int size = menuSection.getInt("size", 54);
        List<Integer> allStorageSlots = new ArrayList<>(menuSection.getIntegerList("allowed-slots"));
        Collections.sort(allStorageSlots);
        int availableAmount = PrimeClans.getInstance().getLevelService().storageSlotsForLevel(clan.level());

        availableAmount = Math.min(availableAmount, allStorageSlots.size());

        Set<Integer> availableSlots = new HashSet<>(allStorageSlots.subList(0, availableAmount));
        Set<Integer> lockedSlots = new HashSet<>(allStorageSlots.subList(availableAmount, allStorageSlots.size()));

        Map<String, String> placeholders = new HashMap<>(Map.of(
                "clan_name", clan.name(),
                "clan_level", String.valueOf(clan.level()),
                "clan_balance", PrimeClans.getInstance().getEconomyManager().format(clan.balance())
        ));

        Map<String, Object> actionContext = Map.of(
                "clan_name", clan.name(),
                "placeholders", placeholders
        );

        Map<Integer, List<MenuAction>> leftActions = new HashMap<>();
        Map<Integer, List<MenuAction>> rightActions = new HashMap<>();

        StorageHolder holder = new StorageHolder(clan.name().toLowerCase(), availableSlots, leftActions, rightActions);
        Inventory inv = Bukkit.createInventory(holder, size, TextColor.parse(title, placeholders));

        if (menuSection.contains("items")) {
            MenuHelper.loadMenuItems(inv, menuSection, placeholders, actionContext, leftActions, rightActions);
        }

        ItemStack[] savedItems = ItemSerializer.fromBase64(base64);
        for (int i = 0; i < savedItems.length && i < size; i++) {
            if (availableSlots.contains(i) && savedItems[i] != null) {
                inv.setItem(i, savedItems[i]);
            }
        }

        if (!lockedSlots.isEmpty() && menuSection.contains("locked-item")) {
            int nextLevel = getNextLevelWithMoreSlots(clan.level(), availableAmount);
            placeholders.put("next_level", String.valueOf(nextLevel));
            Map<String, Object> lockedItemMap = menuSection.getConfigurationSection("locked-item").getValues(false);
            ItemStack lockedItem = ItemBuilder.fromMap(lockedItemMap, placeholders);
            if (lockedItem != null) {
                for (int slot : lockedSlots) {
                    inv.setItem(slot, lockedItem.clone());
                }
            }
        }

        return inv;
    }

    private int getNextLevelWithMoreSlots(int currentLevel, int currentSlots) {
        return PrimeClans.getInstance().getLevelService().levels().values().stream()
                .filter(lvl -> lvl.level() > currentLevel && lvl.storageSlots() > currentSlots)
                .map(ClanLevel::level)
                .findFirst()
                .orElse(currentLevel + 1);
    }

    public void saveAndUnload(String clanName, Inventory inv) {
        Task.async(() -> {
            StorageHolder holder = (StorageHolder) inv.getHolder();
            if (holder == null) return;

            int size = inv.getSize();
            ItemStack[] toSave = new ItemStack[size];
            for (int slot : holder.getAllowedSlots()) {
                if (slot < size) toSave[slot] = inv.getItem(slot);
            }

            String base64 = ItemSerializer.toBase64(toSave);
            db.saveStorage(clanName, base64);
            cache.remove(clanName.toLowerCase());
        });
    }

    public void forceSaveAll() {
        cache.forEach(this::saveAndUnload);
    }
}
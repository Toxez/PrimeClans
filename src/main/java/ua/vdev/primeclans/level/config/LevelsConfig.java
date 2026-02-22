package ua.vdev.primeclans.level.config;

import org.bukkit.configuration.file.YamlConfiguration;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.level.model.ClanLevel;
import ua.vdev.primeclans.level.model.LevelUp;
import ua.vdev.primeclans.level.model.PlaySoundData;

import java.io.File;
import java.util.*;

public class LevelsConfig {

    private final YamlConfiguration config;
    private final Map<String, Long> expSources = new HashMap<>();
    private int defaultMaxMembers = 5;
    private final NavigableMap<Integer, ClanLevel> levels = new TreeMap<>();

    public LevelsConfig() {
        PrimeClans plugin = PrimeClans.getInstance();
        File file = new File(plugin.getDataFolder(), "levels.yml");
        if (!file.exists()) {
            plugin.saveResource("levels.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        reload();
    }

    public void reload() {
        expSources.clear();
        Optional.ofNullable(config.getConfigurationSection("exp"))
                .ifPresent(sec -> sec.getKeys(false).forEach(key ->
                        expSources.put(key, sec.getLong(key))
                ));

        defaultMaxMembers = config.getInt("default-max-members", 5);

        levels.clear();
        List<Map<?, ?>> mapList = config.getMapList("clan-levels");
        for (Map<?, ?> map : mapList) {
            int level = getInt(map, "level", 1);
            int maxMembers = getInt(map, "max-members", defaultMaxMembers);
            long requiredExp = getLong(map, "required-exp", 0L);
            LevelUp levelUp = parseLevelUp(map);

            levels.put(level, new ClanLevel(level, maxMembers, requiredExp, levelUp));
        }

        levels.putIfAbsent(1, new ClanLevel(1, defaultMaxMembers, 0L, new LevelUp(Optional.empty(), List.of())));
    }

    private LevelUp parseLevelUp(Map<?, ?> map) {
        Optional<PlaySoundData> sound = Optional.ofNullable((String) map.get("sound"))
                .flatMap(this::parseSound);

        Object msgObj = map.get("message");
        List<String> messages = new ArrayList<>();

        if (msgObj instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) messages.add(o.toString());
            }
        }

        return new LevelUp(sound, messages);
    }

    private Optional<PlaySoundData> parseSound(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String[] parts = raw.split(";");
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(parts[0].trim().toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;
            return Optional.of(new PlaySoundData(sound, volume, pitch));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private int getInt(Map<?, ?> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return def;
    }

    private long getLong(Map<?, ?> map, String key, long def) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        return def;
    }

    public Map<String, Long> expSources() {
        return Collections.unmodifiableMap(expSources);
    }

    public int defaultMaxMembers() {
        return defaultMaxMembers;
    }

    public NavigableMap<Integer, ClanLevel> levels() {
        return Collections.unmodifiableNavigableMap(levels);
    }
}
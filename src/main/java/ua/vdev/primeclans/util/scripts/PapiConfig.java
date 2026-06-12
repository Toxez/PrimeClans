package ua.vdev.primeclans.util.scripts;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.vlibapi.util.TextColor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PapiConfig {

    private static final String FOLDER = "scripts";
    private final Map<String, Script> compiledScripts = new ConcurrentHashMap<>();
    private final Map<String, String> noClanFallbacks = new HashMap<>();

    public PapiConfig() {
        PrimeClans plugin = PrimeClans.getInstance();
        File folder = new File(plugin.getDataFolder(), FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File docFile = new File(folder, "example.js");
        if (!docFile.exists()) {
            plugin.saveResource(FOLDER + "/example.js", false);
        }

        File configFile = new File(plugin.getDataFolder(), "papi.yml");
        if (!configFile.exists()) {
            plugin.saveResource("papi.yml", false);
        }

        loadSettings(configFile);
        loadScripts(folder);
    }

    private void loadSettings(File configFile) {
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        String[] basePlaceholders = {"name", "level", "people", "balance", "exp", "exp_to_next"};

        for (String key : basePlaceholders) {
            String rawValue = yaml.getString("no-clan." + key, "");

            String coloredValue = LegacyComponentSerializer.legacySection().serialize(TextColor.parse(rawValue));

            noClanFallbacks.put(key, coloredValue);
        }
    }

    private void loadScripts(File folder) {
        File[] files = folder.listFiles(f -> f.isFile() && f.getName().endsWith(".js"));
        if (files == null) return;

        Context cx = Context.enter();
        try {
            cx.setOptimizationLevel(9);
            for (File file : files) {
                String key = file.getName().replace(".js", "").toLowerCase();
                try (FileReader reader = new FileReader(file)) {
                    Script script = cx.compileReader(reader, file.getName(), 1, null);
                    compiledScripts.put(key, script);
                } catch (IOException e) {}
            }
        } finally {
            Context.exit();
        }
    }

    public String getNoClanFallback(String key) {
        return noClanFallbacks.getOrDefault(key, "");
    }

    public Script getScript(String key) {
        return compiledScripts.get(key.toLowerCase());
    }
}
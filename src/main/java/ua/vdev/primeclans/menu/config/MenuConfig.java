package ua.vdev.primeclans.menu.config;

import org.bukkit.configuration.file.YamlConfiguration;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.menu.MenuType;

import java.io.File;
import java.io.IOException;

public class MenuConfig {
    private final File file;
    private YamlConfiguration config;
    public MenuConfig(MenuType type) {
        File menuDir = new File(PrimeClans.getInstance().getDataFolder(), "menu");
        if (!menuDir.exists()) {
            menuDir.mkdirs();
        }
        String fileName = type.getConfigName() + ".yml";
        this.file = new File(menuDir, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                PrimeClans.getInstance().saveResource("menu/" + fileName, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration get() {
        return config;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
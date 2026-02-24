package ua.vdev.primeclans.addon;

import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.api.ClanProvider;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public abstract class AbstractAddon {

    private Logger logger;
    private AddonDescription description;
    private AddonClassLoader classLoader;
    private File dataFolder;
    private boolean enabled = false;

    public AbstractAddon() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof AddonClassLoader loader) {
            loader.initialize(this);
        } else {
            throw new IllegalStateException("AbstractAddon требует AddonClassLoader");
        }
    }

    final void init(AddonDescription description, AddonClassLoader classLoader, AddonLoader addonLoader) {
        this.description = description;
        this.classLoader = classLoader;
        this.logger = Logger.getLogger("PrimeClans#" + description.name());
        this.dataFolder = new File(new File(PrimeClans.getInstance().getDataFolder(), "addons"), description.name());
        this.dataFolder.mkdirs();
    }

    protected abstract void onEnable();
    protected abstract void onDisable();

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            logger.info("Включение " + description.name() + "...");
            onEnable();
        } else {
            logger.info("Выключение " + description.name() + "...");
            onDisable();
        }
    }

    public ClanManager getClanManager() {
        return PrimeClans.getInstance().getClanManager();
    }

    public ClanProvider getClanProvider() {
        return PrimeClans.getInstance().getClanManager();
    }

    public EconomyManager getEconomy() {
        return PrimeClans.getInstance().getEconomyManager();
    }
    public InputStream getResource(String filename) {
        try {
            URL url = classLoader.getResource(filename);
            if (url == null) return null;
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            return conn.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }
    public void saveResource(String resourcePath, boolean replace) {
        File outFile = new File(dataFolder, resourcePath);
        if (outFile.exists() && !replace) return;
        outFile.getParentFile().mkdirs();
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Ресурс не найден в jar: " + resourcePath);
                return;
            }
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
        } catch (IOException e) {
            logger.severe("Ошибка сохранения ресурса " + resourcePath + ": " + e.getMessage());
        }
    }

    public Logger getLogger() { return logger; }
    public AddonDescription getDescription() { return description; }
    public File getDataFolder() { return dataFolder; }
    public boolean isEnabled() { return enabled; }
}
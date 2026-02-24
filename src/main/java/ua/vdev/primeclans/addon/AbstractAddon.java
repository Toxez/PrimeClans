package ua.vdev.primeclans.addon;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.api.ClanProvider;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * <p>Каждый аддон должен
 * <ol>
 *   <li>наследоваться от {@code AbstractAddon}</li>
 *   <li>иметь конструктор без аргументов</li>
 *   <li>содержать файл {@code addon.yml} в корне JAR с полями {@code name} и {@code main}</li>
 * </ol>
 *
 * <p>Пример минимального {@code addon.yml}:
 * <pre>
 * name: Addon
 * main: ua.vdev.addon.Addon
 * version: 1.0
 * author: Tox8729
 * </pre>
 *
 * <p>Пример аддона:
 * <pre>{@code
 * public class Addon extends AbstractAddon {
 *
 *     @Override
 *     protected void onEnable() {
 *         saveDefaultConfig();
 *         getLogger().info("Конфиг загружен " + getConfig().getString("some-key"));
 *         AddonAPI.registerSubCommand(new MySubCommand());
 *     }
 *
 *     @Override
 *     protected void onDisable() {
 *         AddonAPI.unregisterSubCommand("mycommand");
 *     }
 * }
 * }</pre>
 */
public abstract class AbstractAddon {

    private Logger logger;
    private AddonDescription description;
    private AddonClassLoader classLoader;
    private File dataFolder;
    private boolean enabled = false;
    private FileConfiguration config = null;
    private File configFile = null;

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
        this.configFile = new File(dataFolder, "config.yml");
    }

    /**
     * Вызывается при включении аддона
     * Здесь следует регистрировать команды, действия, обработчики событий и тд
     */
    protected abstract void onEnable();

    /**
     * Вызывается при выключении аддона
     * Здесь следует отменять все регистрации освобождать ресурсы
     */
    protected abstract void onDisable();

    /**
     * Включает или выключает аддон
     * Вызывается {@link AddonLoader} автоматически не вызывайте вручную
     *
     * @param enabled {@code true} включить {@code false} выключить
     */
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

    /**
     * Возвращает конфигурацию аддона ({@code config.yml} в папке аддона)
     *
     * <p>При первом обращении конфиг загружается с диска. Если файл не существует
     * возвращается пустая конфигурация. Чтобы создать файл из встроенного шаблона
     * вызовите {@link #saveDefaultConfig()}.
     *
     * @return {@link FileConfiguration} текущая конфигурация аддона
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    /**
     * Перезагружает конфиг с диска
     *
     * <p>Если файла нет сначала пытается загрузить дефолтный {@code config.yml}
     * из джар-файла аддона (если есть) иначе возвращает пустую конфигурацию
     */
    public void reloadConfig() {
        if (configFile == null) return;
        config = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultStream = getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
    }

    /**
     * Сохраняет конфигурацию на диск ({@code config.yml} в папке аддона)
     *
     * <p>Сохраняет <b>текущее состояние</b> объекта, возвращённого {@link #getConfig()}.
     * Используйте {@link FileConfiguration#set(String, Object)} перед вызовом
     * для изменения значений.
     */
    public void saveConfig() {
        if (config == null || configFile == null) return;
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            logger.severe("Ошибка сохранения config.yml: " + e.getMessage());
        }
    }

    /**
     * Сохраняет дефолтный {@code config.yml} из аддона в папку аддона
     * если файл ещё не существует
     *
     * <p>Ничего не делает если конфиг уже существует на диске
     * Чтобы перезаписать используйте {@link #saveResource(String, boolean)}
     * с {@code replace = true}
     */
    public void saveDefaultConfig() {
        if (configFile != null && !configFile.exists()) {
            saveResource("config.yml", false);
        }
        reloadConfig();
    }

    /**
     * Возвращает {@link InputStream} ресурса из аддона
     *
     * @param filename путь к файлу внутри джар (например, {@code "config.yml"})
     * @return поток или {@code null} если ресурс не найден
     */
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

    /**
     * Сохраняет ресурс из аддона в папку данных аддона на диск
     *
     * @param resourcePath относительный путь к ресурсу внутри джар
     * @param replace если {@code true} перезапишет существующий файл
     */
    public void saveResource(String resourcePath, boolean replace) {
        File outFile = new File(dataFolder, resourcePath);
        if (outFile.exists() && !replace) return;
        outFile.getParentFile().mkdirs();
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                logger.warning("Ресурс не найден в джарнике: " + resourcePath);
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

    /**
     * Возвращает {@link ClanManager} полный менеджер кланов
     *
     * <p>Предпочтительнее использовать {@link #getClanProvider()} для read only операций
     *
     * @return {@link ClanManager}
     */
    public ClanManager getClanManager() {
        return PrimeClans.getInstance().getClanManager();
    }

    /**
     * Возвращает {@link ClanProvider} — read only интерфейс доступа к кланам
     * Рекомендован для аддонов которым не нужно изменять состояние кланов
     *
     * @return {@link ClanProvider}
     */
    public ClanProvider getClanProvider() {
        return PrimeClans.getInstance().getClanManager();
    }

    /**
     * Возвращает {@link EconomyManager}
     *
     * @return {@link EconomyManager}
     */
    public EconomyManager getEconomy() {
        return PrimeClans.getInstance().getEconomyManager();
    }

    /**
     * Возвращает логгер аддона с именем вида {@code PrimeClans#AddonName}
     *
     * @return {@link Logger}
     */
    public Logger getLogger() { return logger; }

    /**
     * Возвращает метаданные аддона из {@code addon.yml}
     *
     * @return {@link AddonDescription}
     */
    public AddonDescription getDescription() { return description; }

    /**
     * Возвращает папку данных аддона: {@code plugins/PrimeClans/addons/<AddonName>/}.
     * Папка создаётся автоматически при инициализации.
     *
     * @return {@link File} папка данных
     */
    public File getDataFolder() { return dataFolder; }

    /**
     * Проверяет включён ли аддон в данный момент
     *
     * @return {@code true} если аддон активен
     */
    public boolean isEnabled() { return enabled; }
}
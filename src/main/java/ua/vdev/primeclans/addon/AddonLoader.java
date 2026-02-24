package ua.vdev.primeclans.addon;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class AddonLoader implements Closeable {

    private static final Logger LOGGER = Logger.getLogger("PrimeClans#AddonLoader");

    private final File addonsFolder;
    private final Map<String, AbstractAddon> addons = new ConcurrentHashMap<>();
    private final List<AddonClassLoader> classLoaders = new CopyOnWriteArrayList<>();

    public AddonLoader(File pluginDataFolder) {
        this.addonsFolder = new File(pluginDataFolder, "addons");
        this.addonsFolder.mkdirs();
    }

    public void findAndLoad() {
        File[] jars = addonsFolder.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

        if (jars == null || jars.length == 0) {
            LOGGER.info("Аддонов не найдено");
            return;
        }

        Map<String, AddonDescription> descriptions = new LinkedHashMap<>();
        Map<String, File> jarFiles = new LinkedHashMap<>();

        for (File jar : jars) {
            try {
                AddonDescription desc = readDescription(jar);
                descriptions.put(desc.name().toLowerCase(), desc);
                jarFiles.put(desc.name().toLowerCase(), jar);
            } catch (Exception e) {
                LOGGER.severe("Ошибка чтения addon.yml из " + jar.getName() + ": " + e.getMessage());
            }
        }

        List<String> sorted = sortByDependencies(descriptions);

        for (String name : sorted) {
            try {
                AddonDescription desc = descriptions.get(name);
                File jar = jarFiles.get(name);

                boolean depsOk = desc.depend().stream()
                        .allMatch(dep -> addons.containsKey(dep.toLowerCase()));

                if (!depsOk) {
                    LOGGER.severe("Аддон " + desc.name() + " не загружен — не найдены зависимости: " + desc.depend());
                    continue;
                }

                AddonClassLoader cl = new AddonClassLoader(
                        getClass().getClassLoader(), desc, this, jar
                );
                classLoaders.add(cl);
                addons.put(desc.name().toLowerCase(), cl.addon);

                LOGGER.info("Найден аддон: " + desc.name() + " v" + desc.version() + " by " + desc.authors());

            } catch (Exception e) {
                LOGGER.severe("Ошибка загрузки аддона " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void enableAll() {
        addons.values().forEach(addon -> {
            try {
                addon.setEnabled(true);
            } catch (Exception e) {
                LOGGER.severe("Ошибка включения " + addon.getDescription().name() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void disableAll() {
        List<AbstractAddon> reversed = new ArrayList<>(addons.values());
        Collections.reverse(reversed);
        reversed.forEach(addon -> {
            try {
                addon.setEnabled(false);
            } catch (Exception e) {
                LOGGER.severe("Ошибка выключения " + addon.getDescription().name() + ": " + e.getMessage());
            }
        });
    }

    private AddonDescription readDescription(File jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar)) {
            JarEntry entry = jarFile.getJarEntry("addon.yml");
            if (entry == null) throw new IOException(jar.getName() + " не содержит addon.yml");
            try (InputStream in = jarFile.getInputStream(entry);
                 InputStreamReader reader = new InputStreamReader(in)) {
                return new AddonDescription(YamlConfiguration.loadConfiguration(reader));
            }
        }
    }

    private List<String> sortByDependencies(Map<String, AddonDescription> all) {
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String name : all.keySet()) {
            resolve(name, all, sorted, visited, new HashSet<>());
        }
        return sorted;
    }

    private void resolve(String name, Map<String, AddonDescription> all,
                         List<String> sorted, Set<String> visited, Set<String> chain) {
        if (visited.contains(name)) return;
        if (chain.contains(name)) {
            LOGGER.severe("Циклическая зависимость: " + chain + " -> " + name);
            return;
        }
        AddonDescription desc = all.get(name);
        if (desc == null) return;
        chain.add(name);
        desc.depend().forEach(dep -> resolve(dep.toLowerCase(), all, sorted, visited, chain));
        chain.remove(name);
        visited.add(name);
        sorted.add(name);
    }

    @Nullable
    public Class<?> getClassByName(String name, boolean resolve) {
        for (AddonClassLoader cl : classLoaders) {
            try { return cl.loadClass0(name, resolve, false); }
            catch (ClassNotFoundException ignore) {}
        }
        return null;
    }

    public Map<String, AbstractAddon> getAddons() {
        return Collections.unmodifiableMap(addons);
    }

    @Override
    public void close() throws IOException {
        disableAll();
        addons.clear();
        for (AddonClassLoader cl : classLoaders) cl.close();
        classLoaders.clear();
    }
}
package ua.vdev.primeclans.addon;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AddonDescription {

    private final String name;
    private final String mainClass;
    private final String version;
    private final String description;
    private final String apiVersion;
    private final Set<String> authors;
    private final List<String> depend;
    private final List<String> softDepend;

    public AddonDescription(YamlConfiguration yml) {
        name = Objects.requireNonNull(yml.getString("name"), "addon.yml missing 'name'!");
        mainClass = Objects.requireNonNull(yml.getString("main"), "addon.yml missing 'main'!");
        version = yml.getString("version", "1.0");
        description = yml.getString("description", "");
        apiVersion = yml.getString("api-version", "1.0");
        depend = yml.getStringList("depend");
        softDepend = yml.getStringList("soft-depend");

        authors = new HashSet<>();
        authors.addAll(yml.getStringList("authors"));
        String single = yml.getString("author");
        if (single != null) authors.add(single);
    }

    public String name() { return name; }
    public String mainClass() { return mainClass; }
    public String version() { return version; }
    public String description() { return description; }
    public String apiVersion() { return apiVersion; }
    public Set<String> authors() { return authors; }
    public List<String> depend() { return depend; }
    public List<String> softDepend() { return softDepend; }
}
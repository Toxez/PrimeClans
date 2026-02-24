package ua.vdev.primeclans.api.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClanCommandRegistry {

    private static final Map<String, AddonSubCommand> commands = new HashMap<>();

    public static void register(AddonSubCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public static Optional<AddonSubCommand> get(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public static Map<String, AddonSubCommand> getAll() {
        return Collections.unmodifiableMap(commands);
    }

    public static void unregister(String name) {
        commands.remove(name.toLowerCase());
    }

    public static void clear() {
        commands.clear();
    }
}
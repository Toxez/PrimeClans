package ua.vdev.primeclans.menu.action;

import ua.vdev.primeclans.menu.action.impl.*;
import ua.vdev.primeclans.perm.ClanPerm;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;
import java.util.*;
import java.util.stream.Collectors;

public class ActionFactory {

    public static List<MenuAction> create(List<String> rawActions, Map<String, Object> context) {
        if (rawActions == null || rawActions.isEmpty()) return Collections.emptyList();
        String clanName = getContextValue(context, "clan_name", String.class).orElse(null);
        Map<String, String> placeholders = getSafePlaceholders(context);
        Optional<UUID> targetUuidOpt = getContextValue(context, "target_uuid", String.class)
                .flatMap(s -> {
                    try { return Optional.of(UUID.fromString(s)); }
                    catch (IllegalArgumentException e) { return Optional.empty(); }
                });

        return rawActions.stream()
                .map(String::trim)
                .map(raw -> ActionType.fromRaw(raw)
                        .flatMap(type -> buildAction(type, raw, clanName, placeholders, targetUuidOpt)))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private static Optional<MenuAction> buildAction(ActionType type, String raw, String clanName, Map<String, String> placeholders, Optional<UUID> targetUuidOpt) {
        String arg = raw.substring(type.getPrefix().length()).trim();
        String processedArg = replacePlaceholders(arg, placeholders);

        return switch (type) {
            case CLOSE -> Optional.of(new Close());
            case MESSAGE -> Optional.of(new Message(processedArg, placeholders));
            case OPEN_MENU -> Optional.of(new OpenMenu(processedArg));
            case CREATE_CLAN -> Optional.ofNullable(clanName).map(CreateClan::new);
            case DELETE_CLAN -> Optional.ofNullable(clanName).map(DeleteClan::new);
            case PLAY_SOUND -> parseSound(processedArg);
            case PLAYER_COMMAND -> Optional.of(new Player(processedArg));
            case CONSOLE_COMMAND -> Optional.of(new Console(processedArg));
            case TITLE -> parseTitle(processedArg);
            case ACTIONBAR -> parseActionBar(processedArg);
            case PLAYER_EFFECT -> parseEffect(processedArg);
            case ENABLE_GLOW -> Optional.of(new EnableGlow());
            case DISABLE_GLOW -> Optional.of(new DisableGlow());
            case SET_GLOW_COLOR -> Optional.of(new SetGlowColor(processedArg));
            case SET_MEMBER_GLOW_COLOR -> targetUuidOpt.map(uuid -> new SetMemberGlowColor(uuid, processedArg));
            case RESET_MEMBER_GLOW_COLOR -> targetUuidOpt.map(ResetMemberGlowColor::new);
            case TOGGLE_PERM -> targetUuidOpt.flatMap(uuid -> ClanPerm.of(processedArg).map(perm -> new TogglePerm(uuid, perm)));
        };
    }

    private static Optional<MenuAction> parseSound(String arg) {
        try {
            String[] parts = arg.split(";");
            if (parts.length >= 3) {
                Sound sound = Sound.valueOf(parts[0].trim().toUpperCase());
                float volume = Float.parseFloat(parts[1].trim());
                float pitch  = Float.parseFloat(parts[2].trim());
                return Optional.of(new PlaySound(sound, volume, pitch));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<MenuAction> parseTitle(String arg) {
        try {
            String[] parts = arg.split(";");
            if (parts.length >= 2) {
                String title    = parts[0].trim();
                String subtitle = parts.length > 1 ? parts[1].trim() : "";
                int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 10;
                int stay = parts.length > 3 ? Integer.parseInt(parts[3].trim()) : 70;
                int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 20;
                return Optional.of(new Title(title, subtitle, fadeIn, stay, fadeOut));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<MenuAction> parseActionBar(String arg) {
        try {
            String[] parts = arg.split(";");
            String message = parts[0].trim();
            int duration = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 60;
            return Optional.of(new ActionBar(message, duration));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<MenuAction> parseEffect(String arg) {
        try {
            String[] parts = arg.split(";");
            PotionEffectType effectType = PotionEffectType.getByName(parts[0].trim().toUpperCase());
            if (effectType == null) return Optional.empty();
            int duration = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 200;
            int amplifier = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
            return Optional.of(new Effect(effectType, duration, amplifier));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null) return text;
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static <T> Optional<T> getContextValue(Map<String, Object> context, String key, Class<T> type) {
        return Optional.ofNullable(context.get(key))
                .filter(type::isInstance)
                .map(type::cast);
    }

    private static Map<String, String> getSafePlaceholders(Map<String, Object> context) {
        Object value = context.get("placeholders");
        if (value instanceof Map<?, ?> map) {
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> {
                if (k instanceof String sk && v instanceof String sv) result.put(sk, sv);
            });
            return result;
        }
        return Collections.emptyMap();
    }
}
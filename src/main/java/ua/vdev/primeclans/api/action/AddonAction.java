package ua.vdev.primeclans.api.action;

import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Кастомное действие вызываемое из конфигов меню кланов
 *
 * <p>Регистрируется через {@link ActionRegistry#register(String, AddonAction)}
 * или через {@link ua.vdev.primeclans.api.AddonAPI#registerAction(String, AddonAction)}.
 *
 * <p>Пример регистрации:
 * <pre>{@code
 * AddonAPI.registerAction("[give-boost]", (player, arg, ctx) -> {
 *     int seconds = Integer.parseInt(arg);
 *     player.setWalkSpeed(0.4f);
 *     Bukkit.getScheduler().runTaskLater(plugin, () ->
 *         player.setWalkSpeed(0.2f), seconds * 20L);
 * });
 * }</pre>
 *
 * <p>Пример в ямл:
 * <pre>
 * left_click_actions:
 *   - "[give-boost] 10"
 * </pre>
 */
@FunctionalInterface
public interface AddonAction {

    /**
     * Выполняет действие
     *
     * @param player игрок который совершил клик
     * @param arg аргумент после префикса: {@code "[prefix] <arg>"}; может быть пустой строкой
     * @param context карта контекста доступная в момент клика; содержит ключи:
     * <ul>
     *  <li>{@code "clan_name"} — {@link String} имя клана игрока (если есть)</li>
     *  <li>{@code "target_uuid"} — {@link String} UUID целевого игрока (если применимо)</li>
     *  <li>{@code "placeholders"} — {@code Map<String, String>} плейсхолдеры меню</li>
     * </ul>
     */
    void execute(Player player, String arg, Map<String, Object> context);
}
package ua.vdev.primeclans.api.variable;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

/**
 * Кастомная локальная переменная для скриптов в меню кланов.
 *
 * <p>Регистрируется ТОЛЬКО через {@link ua.vdev.primeclans.api.AddonAPI#registerVariable(String, ContextVariable)}.
 *
 * <p>Пример регистрации в аддоне
 * <pre>{@code
 * AddonAPI.registerVariable("player_kills", (player, clan) -> {
 * return String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS));
 * });
 * }</pre>
 *
 * <p>Теперь в ямл можно писать:
 * <pre>
 * - if:
 * condition: "player_kills >= 100"
 * </pre>
 */
@FunctionalInterface
public interface ContextVariable {

    /**
     * Возвращает значение переменной в виде строки.
     *
     * @param player игрок, для которого вычисляется условие
     * @param clan клан игрока (может быть null, если игрок не в клане!)
     * @return вычисленное значение (например, "true", "150", "какой-то текст")
     */
    String resolve(Player player, Clan clan);
}
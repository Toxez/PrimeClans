package ua.vdev.primeclans.api.requirement;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

import java.util.Map;

/**
 * Кастомное требование для системы {@code requirements} в конфигах меню кланов
 *
 * <p>Регистрируется через {@link RequirementRegistry#register(String, AddonRequirement)}
 * или через {@link ua.vdev.primeclans.api.AddonAPI#registerRequirement(String, AddonRequirement)}.
 *
 * <p>Пример ямл с кастомным требованием
 * <pre>
 * requirements:
 *   left_click:
 *     mode: ALL
 *     conditions:
 *       - type: MY_KILLS
 *         min: 100
 *     deny_actions:
 *       - "[message] Недостаточно убийств"
 * </pre>
 *
 * <p>Пример регистрации:
 * <pre>{@code
 * AddonAPI.registerRequirement("MY_KILLS", (player, clan, params) -> {
 *     int min = ((Number) params.getOrDefault("min", 0)).intValue();
 *     return getKills(player) >= min;
 * });
 * }</pre>
 */
@FunctionalInterface
public interface AddonRequirement {

    /**
     * Проверяет выполняется ли требование для данного игрока
     *
     * @param player игрок, совершивший клик
     * @param clan клан игрока может быть {@code null} если игрок не в клане
     * @param params дополнительные параметры из YAML-секции условия (например, {@code "min"}, {@code "max"}, {@code "amount"})
     * @return {@code true} если требование выполнено
     */
    boolean check(Player player, Clan clan, Map<String, Object> params);
}
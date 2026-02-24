package ua.vdev.primeclans.api.requirement;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

import java.util.Map;

@FunctionalInterface
public interface AddonRequirement {
    /**
     * @param player игрок
     * @param clan клан игрока (может быть null если requiresClan = false)
     * @param params доп параметры из yml (amount min max и тд.....)
     */
    boolean check(Player player, Clan clan, Map<String, Object> params);
}
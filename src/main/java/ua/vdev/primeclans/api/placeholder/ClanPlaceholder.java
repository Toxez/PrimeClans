package ua.vdev.primeclans.api.placeholder;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

@FunctionalInterface
public interface ClanPlaceholder {
    String resolve(Clan clan, Player player);
}
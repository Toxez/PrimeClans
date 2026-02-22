package ua.vdev.primeclans.api;

import ua.vdev.primeclans.model.Clan;

import java.util.Optional;
import java.util.UUID;
import java.util.Collection;

public interface ClanProvider {
    Optional<Clan> getClan(String name);
    Optional<Clan> getPlayerClan(UUID uuid);
    Collection<String> getClanNames();
    default boolean isSameClan(UUID p1, UUID p2) {
        Optional<Clan> clan1 = getPlayerClan(p1);
        return clan1.isPresent() && clan1.equals(getPlayerClan(p2));
    }
}
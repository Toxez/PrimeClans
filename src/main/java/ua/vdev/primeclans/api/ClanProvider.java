package ua.vdev.primeclans.api;

import ua.vdev.primeclans.model.Clan;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * интерфейс доступа к данным кланов
 *
 * <p>Лучше для аддонов и внешних плагинов которым нужно только
 * читать данные. Получить экземпляр можно несколькими способами:
 *
 * <pre>{@code
 * // Способ 1 — через AddonAPI (для аддонов)
 * ClanProvider provider = AddonAPI.getClanProvider();
 *
 * // Способ 2 — через Bukkit ServicesManager (для сторонних плагинов)
 * ClanProvider provider = Bukkit.getServicesManager()
 *         .getRegistration(ClanProvider.class)
 *         .getProvider();
 *
 * // Способ 3 — в AbstractAddon
 * ClanProvider provider = getClanProvider();
 * }</pre>
 */
public interface ClanProvider {

    /**
     * Возвращает клан по имени (регистронезависимо)
     *
     * @param name имя клана
     * @return {@link Optional} с кланом или пустой если не найден
     */
    Optional<Clan> getClan(String name);

    /**
     * Возвращает клан в котором состоит игрок
     *
     * @param uuid UUID игрока
     * @return {@link Optional} с кланом или пустой если игрок не в клане
     */
    Optional<Clan> getPlayerClan(UUID uuid);

    /**
     * Возвращает коллекцию имён всех зарегистрированных кланов (нижний регистр)
     *
     * @return неизменяемая коллекция имён кланов
     */
    Collection<String> getClanNames();

    /**
     * Проверяет состоят ли два игрока в одном клане
     *
     * @param p1 UUID первого игрока
     * @param p2 UUID второго игрока
     * @return {@code true} если оба в одном клане
     */
    default boolean isSameClan(UUID p1, UUID p2) {
        Optional<Clan> clan1 = getPlayerClan(p1);
        return clan1.isPresent() && clan1.equals(getPlayerClan(p2));
    }
}
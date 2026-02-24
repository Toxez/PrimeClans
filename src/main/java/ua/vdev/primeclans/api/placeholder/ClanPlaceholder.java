package ua.vdev.primeclans.api.placeholder;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.model.Clan;

/**
 * Кастомный плейсхолдер для конфигов меню кланов
 *
 * <p>Регистрируется через {@link PlaceholderRegistry#register(String, ClanPlaceholder)}
 * или через {@link ua.vdev.primeclans.api.AddonAPI#registerPlaceholder(String, ClanPlaceholder)}.
 *
 * <p>После регистрации с ключом {@code "my_key"} плейсхолдер
 * доступен в ямл как {@code {my_key}}.
 *
 * <p>Пример:
 * <pre>{@code
 * AddonAPI.registerPlaceholder("clan_kills", (clan, player) ->
 *     String.valueOf(getKills(clan.name()))
 * );
 * }</pre>
 *
 * <p>Будет как:
 * <pre>
 * name: "Убийств клана: {clan_kills}"
 * </pre>
 */
@FunctionalInterface
public interface ClanPlaceholder {

    /**
     * Вычисляет значение плейсхолдера
     *
     * @param clan клан игрока (гарантированно не {@code null})
     * @param player игрок для которого открывается меню
     * @return строковое значение плейсхолдера не должно быть {@code null}
     */
    String resolve(Clan clan, Player player);
}
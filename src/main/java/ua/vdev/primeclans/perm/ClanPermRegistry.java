package ua.vdev.primeclans.perm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр всех прав клана — встроенных и зарегистрированных аддонами.
 *
 * <p>Встроенные права (из {@link ClanPerm}) регистрируются автоматически
 * в статическом блоке с configKey совместимыми с player-perm.yml.
 *
 * <p>Аддоны регистрируют права через
 * {@link ua.vdev.primeclans.api.AddonAPI#registerPerm(ClanPermEntry)}.
 *
 * <p>Пример регистрации в аддоне:
 * <pre>{@code
 * AddonAPI.registerPerm(ClanPermEntry.of("HOME_SET", "home_set", "<green>Установка дома"));
 * }</pre>
 *
 * <p>После регистрации плейсхолдеры {@code {perm_home_set_material}} и
 * {@code {perm_home_set_status}} становятся доступны в player-perm.yml.
 */
public final class ClanPermRegistry {

    private static final Map<String, ClanPermEntry> registry = new LinkedHashMap<>();

    static {
        register(ClanPermEntry.of("KICK_MEMBERS","kick","<red>Кик участников","Право кикать участников клана"));
        register(ClanPermEntry.of("INVITE_MEMBERS","invite","<green>Приглашение","Право приглашать игроков в клан"));
        register(ClanPermEntry.of("CLAN_CHAT","chat","<yellow>Клан чат","Право писать в клановый чат"));
        register(ClanPermEntry.of("INVEST_BALANCE","invest","<gold>Пополнение казны","Право вносить деньги в казну"));
        register(ClanPermEntry.of("WITHDRAW_BALANCE","withdraw","<gold>Снятие с казны","Право снимать деньги из казны"));
        register(ClanPermEntry.of("TOGGLE_PVP","pvp","<red>ПВП переключение","Право включать/выключать клановое ПВП"));
        register(ClanPermEntry.of("MANAGE_GLOW","glow","<aqua>Управление свечением","Право управлять свечением клана"));
        register(ClanPermEntry.of("MANAGE_MEMBER_GLOW","member_glow","<aqua>Свечение участников","Право менять цвет свечения участников"));
    }

    private ClanPermRegistry() {}

    /**
     * Регистрирует право. Если право с таким ключом уже есть — перезаписывает
     *
     * @param entry метаданные права
     */
    public static void register(ClanPermEntry entry) {
        registry.put(entry.key(), entry);
    }

    /**
     * Отменяет регистрацию права по ключу
     * Встроенные права ({@link ClanPerm}) нельзя удалить
     *
     * @param key ключ права (регистронезависимо)
     */
    public static void unregister(String key) {
        if (isCorePerm(key)) return;
        registry.remove(key.toUpperCase());
    }

    /**
     * Проверяет, зарегистрировано ли право с данным ключом
     *
     * @param key ключ права (регистронезависимо)
     */
    public static boolean isRegistered(String key) {
        return registry.containsKey(key.toUpperCase());
    }

    /**
     * Возвращает метаданные права по ключу
     *
     * @param key ключ права (регистронезависимо)
     * @return {@link Optional} с {@link ClanPermEntry} или пустой
     */
    public static Optional<ClanPermEntry> get(String key) {
        return Optional.ofNullable(registry.get(key.toUpperCase()));
    }

    /**
     * Возвращает все зарегистрированные права в порядке регистрации
     *
     * @return неизменяемая карта {@code ключ - запись}
     */
    public static Map<String, ClanPermEntry> getAll() {
        return Collections.unmodifiableMap(registry);
    }

    /** Является ли право встроенным (из {@link ClanPerm}) */
    private static boolean isCorePerm(String key) {
        try {
            ClanPerm.valueOf(key.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
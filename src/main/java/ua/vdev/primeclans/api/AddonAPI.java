package ua.vdev.primeclans.api;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.api.action.ActionRegistry;
import ua.vdev.primeclans.api.action.AddonAction;
import ua.vdev.primeclans.api.command.AddonSubCommand;
import ua.vdev.primeclans.api.command.ClanCommandRegistry;
import ua.vdev.primeclans.api.event.ClanEventBus;
import ua.vdev.primeclans.api.menu.MenuRegistry;
import ua.vdev.primeclans.api.placeholder.ClanPlaceholder;
import ua.vdev.primeclans.api.placeholder.PlaceholderRegistry;
import ua.vdev.primeclans.api.requirement.AddonRequirement;
import ua.vdev.primeclans.api.requirement.RequirementRegistry;
import ua.vdev.primeclans.menu.Menu;
import ua.vdev.primeclans.model.Clan;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Пример использования:
 * <pre>{@code
 * // onEnable()
 * AddonAPI.registerAction("boost", (player, arg, ctx) -> {
 *     player.setWalkSpeed(0.4f);
 * });
 *
 * AddonAPI.registerSubCommand(new AddonSubCommand() {
 *     public String getName() { return "boost"; }
 *     public void execute(Player player, String[] args) { ... }
 * });
 *
 * // onDisable()
 * AddonAPI.unregisterAction("boost");
 * AddonAPI.unregisterSubCommand("boost");
 * }</pre>
 */
public final class AddonAPI {

    private AddonAPI() {}

    /**
     * Регистрирует кастомное действие для использования в конфигах меню
     *
     * <p>Пример в ямл
     * <pre>
     * left_click_actions:
     *   - "[myprefix] аргумент"
     * </pre>
     *
     * @param prefix префикс действия в нижнем регистре включая скобки например {@code "[kill]"}
     * @param action обработчик действия
     * @see AddonAction
     */
    public static void registerAction(String prefix, AddonAction action) {
        ActionRegistry.register(prefix, action);
    }

    /**
     * Отменяет регистрацию действия по префиксу
     *
     * @param prefix префикс переданный при регистрации
     */
    public static void unregisterAction(String prefix) {
        ActionRegistry.unregister(prefix);
    }

    /**
     * Регистрирует подкоманду для команды {@code /clan}
     *
     * <p>Имя подкоманды должно быть уникальным и в нижнем регистре
     * Подкоманда будет показана в таб комплите автоматически
     * в соответствии с флагами {@link AddonSubCommand#requiresClan()} и
     * {@link AddonSubCommand#requiresNoClan()}
     *
     * @param command реализация подкоманды
     * @see AddonSubCommand
     */
    public static void registerSubCommand(AddonSubCommand command) {
        ClanCommandRegistry.register(command);
    }

    /**
     * Отменяет регистрацию подкоманды по имени
     *
     * @param name имя подкоманды (без учёта регистра)
     */
    public static void unregisterSubCommand(String name) {
        ClanCommandRegistry.unregister(name);
    }

    /**
     * Регистрирует кастомное меню открываемое через {@code [open_menu] id}
     *
     * @param id уникальный идентификатор меню (нижний регистр)
     * @param factory функция создающая экземпляр {@link Menu} по клану игрока
     * @see MenuRegistry
     */
    public static void registerMenu(String id, Function<Clan, Menu> factory) {
        MenuRegistry.register(id, factory);
    }

    /**
     * Отменяет регистрацию меню по идентификатору
     *
     * @param id идентификатор меню
     */
    public static void unregisterMenu(String id) {
        MenuRegistry.unregister(id);
    }

    /**
     * Открывает зарегистрированное меню для игрока
     *
     * @param player игрок которому открывается меню
     * @param menuId идентификатор меню
     * @param clan клан передаваемый в фабрику меню
     */
    public static void openMenu(Player player, String menuId, Clan clan) {
        MenuRegistry.open(player, menuId, clan);
    }

    /**
     * Регистрирует плейсхолдер доступный внутри конфигов меню кланов
     *
     * <p>Ключ используется в тексте без фигурных скобок например
     * ключ {@code "placeholder"} → {@code "{placeholder}"} в ямл
     *
     * @param key уникальный ключ плейсхолдера (нижний регистр)
     * @param placeholder функция резольвер
     * @see ClanPlaceholder
     */
    public static void registerPlaceholder(String key, ClanPlaceholder placeholder) {
        PlaceholderRegistry.register(key, placeholder);
    }

    /**
     * Отменяет регистрацию плейсхолдера по ключу
     *
     * @param key ключ плейсхолдера
     */
    public static void unregisterPlaceholder(String key) {
        PlaceholderRegistry.unregister(key);
    }

    /**
     * Регистрирует кастомное требование для системы {@code requirements} в конфигах меню
     *
     * <p>Тип указывается в ямл как {@code type: MY_TYPE} (регистронезависимо)
     *
     * @param type строковый тип требования (будет приведён к верхнему регистру)
     * @param requirement реализация проверки
     * @see AddonRequirement
     */
    public static void registerRequirement(String type, AddonRequirement requirement) {
        RequirementRegistry.register(type, requirement);
    }

    /**
     * Подписывается на событие создания клана
     *
     * @param handler обработчик события
     */
    public static void onClanCreate(Consumer<ClanEventBus.ClanCreateEvent> handler) {
        ClanEventBus.onClanCreate(handler);
    }

    /**
     * Подписывается на событие удаления клана
     *
     * @param handler обработчик события
     */
    public static void onClanDelete(Consumer<ClanEventBus.ClanDeleteEvent> handler) {
        ClanEventBus.onClanDelete(handler);
    }

    /**
     * Подписывается на событие вступления игрока в клан
     *
     * @param handler обработчик события
     */
    public static void onMemberJoin(Consumer<ClanEventBus.MemberJoinEvent> handler) {
        ClanEventBus.onMemberJoin(handler);
    }

    /**
     * Подписывается на событие выхода игрока из клана
     *
     * @param handler обработчик события
     */
    public static void onMemberLeave(Consumer<ClanEventBus.MemberLeaveEvent> handler) {
        ClanEventBus.onMemberLeave(handler);
    }

    /**
     * Подписывается на событие повышения уровня клана
     *
     * @param handler обработчик события
     */
    public static void onLevelUp(Consumer<ClanEventBus.ClanLevelUpEvent> handler) {
        ClanEventBus.onLevelUp(handler);
    }

    /**
     * Подписывается на событие получения опыта кланом
     *
     * @param handler обработчик события
     */
    public static void onExpGain(Consumer<ClanEventBus.ExpGainEvent> handler) {
        ClanEventBus.onExpGain(handler);
    }

    /**
     * Подписывается на событие переключения пвп в клане
     *
     * @param handler обработчик события
     */
    public static void onPvpToggle(Consumer<ClanEventBus.PvpToggleEvent> handler) {
        ClanEventBus.onPvpToggle(handler);
    }

    /**
     * Подписывается на событие изменения баланса клана
     *
     * @param handler обработчик события
     */
    public static void onBalanceChange(Consumer<ClanEventBus.BalanceChangeEvent> handler) {
        ClanEventBus.onBalanceChange(handler);
    }

    /**
     * Возвращает {@link ClanProvider} — read only интерфейс доступа к кланам
     *
     * <p>Используйте его вместо прямого обращения к {@code ClanManager},
     * чтобы не зависеть от внутренней реализации.
     *
     * @return экземпляр {@link ClanProvider}
     */
    public static ClanProvider getClanProvider() {
        return ua.vdev.primeclans.PrimeClans.getInstance().getClanManager();
    }
}
package ua.vdev.primeclans.api.command;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Подкоманда для команды {@code /clan} добавляемая аддоном
 *
 * <p>Регистрируется через {@link ClanCommandRegistry#register(AddonSubCommand)}
 * или через {@link ua.vdev.primeclans.api.AddonAPI#registerSubCommand(AddonSubCommand)}.
 *
 * <p>Пример
 * <pre>{@code
 * public class BoostCommand implements AddonSubCommand {
 *
 *     @Override
 *     public String getName() { return "boost"; }
 *
 *     @Override
 *     public boolean requiresClan() { return true; }
 *
 *     @Override
 *     public void execute(Player player, String[] args) {
 *         player.sendMessage("Типо активирован");
 *     }
 *
 *     @Override
 *     public List<String> tabComplete(Player player, String[] args) {
 *         return List.of("10", "30", "60");
 *     }
 * }
 * }</pre>
 *
 * <p>Используется как: {@code /clan boost}
 */
public interface AddonSubCommand {

    /**
     * Возвращает имя подкоманды в нижнем регистре
     * Например, {@code "boost"} будет {@code /clan boost}
     *
     * @return имя подкоманды
     */
    String getName();

    /**
     * Выполняет логику подкоманды
     *
     * @param player игрок вызвавший команду
     * @param args полный массив аргументов: {@code args[0]} — имя подкоманды
     * {@code args[1..n]} — дополнительные аргументы
     */
    void execute(Player player, String[] args);

    /**
     * Возвращает список вариантов для таб комплита
     *
     * @param player игрок нажавший таб
     * @param args текущие аргументы строки ввода
     * @return список подсказок (может быть пустым)
     */
    default List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }

    /**
     * Если {@code true} — подкоманда отображается в таб комплите
     * только игрокам состоящим в клане
     * По умолчанию {@code true}.
     *
     * @return флаг требования клана
     */
    default boolean requiresClan() {
        return true;
    }

    /**
     * Если {@code true} — подкоманда отображается в таб комплите
     * только игрокам <b>не</b> состоящим в клане.
     * По умолчанию {@code false}.
     *
     * @return флаг требования отсутствия клана
     */
    default boolean requiresNoClan() {
        return false;
    }
}
//
// ЭТО СДЕЛАНО ДЛЯ СОЗДАНИЯ СВОИХ ПЛЕЙСХОЛДЕРОВ, И ПОКА ЧТО НАХОДИТЬСЯ В БЕТЕ
// НЕ СОЗДАВАЙТЕ СЛИШКОМ СЛОЖНЫЕ СКРИПТЫ, И ПРИ КАКОЙ ЛИБО ОШИБКЕ ПИШИТЕ @Tox_8729
//
// Главное правило
// Какое выражение выполнится в скрипте последним — то и вернется в игру
// Писать слово return в конце строк НЕ НУЖНО ПОЖАЛУЙСТА
//
// объект player
//
// Базовые данные
// player.getName() - возвращает ник игрока
// player.getUniqueId() - возвращает UUID игрока
// player.getUniqueId().toString() - UUID в текстовом виде
//
// Состояние игрока
// player.getHealth() - текущее здоровье игрока
// player.getFoodLevel() - уровень голода
// player.getLevel() - уровень опыта игрока
// player.getGameMode().name() - режим игры
//
// Местоположение
// player.getWorld().getName() - название мира, где находится игрок
// player.getLocation().getBlockX() - координата X
//
// Права и пермишены:
// player.hasPermission("право") — проверяет право
//
//
// РАЗБОР ОБЪЕКТА clan (ua.vdev.primeclans.model.Clan)
//
// ВНИМАНИЕ Если игрок не состоит в клане этот объект равен null
// Любая попытка вызвать метод у null (например, clan.name()) вызовет ошибку
// начинай скрипт с проверки if (clan == null) {}
//
// Доступные методы из
// clan.name() - название клана
// clan.level() - уровень клана
// clan.exp() - текущий опыт клана
// clan.balance() - баланс казны клана
// clan.isOwner(player.getUniqueId()) - является ли этот игрок владельцем
//
//
// clan.members() - возвращает джава коллекцию Set<UUID> всех участников
//
// Как узнать количество человек
// clan.members().size() - размер коллекции
//
// Как проверить в сети ли владелец клана
// var ownerUuid = clan.owner(); получает UUID владельца
// var ownerPlayer = org.bukkit.Bukkit.getPlayer(ownerUuid); ищет игрока на сервере
// (ownerPlayer != null) - если не null значит создатель сейчас на сервере
//
//
// api (ua.vdev.primeclans.PrimeClans)
//
//
// Пример работы с уровнями
// var levelService = api.getLevelService(); получаем ClanLevelService
// var nextExp = levelService.requiredExpForLevel(clan.level() + 1); сколько опыта надо для следующего уровня
//
//
// Плейсхолдер будет %primeclans_example%
// так как береться название файла + приписка в начале primeclans_
//
if (clan == null) {
    "&7Нет клана";
} else {
    var isLeader = clan.isOwner(player.getUniqueId());
    if (isLeader) {
        "&e[Лидер] &f" + clan.name();
    } else {
        "&a[Участник] &f" + clan.name();
    }
}

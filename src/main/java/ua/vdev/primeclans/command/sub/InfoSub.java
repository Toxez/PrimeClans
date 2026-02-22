package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.level.ClanLevelService;
import ua.vdev.primeclans.level.model.ClanLevel;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.util.Lang;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InfoSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(
                clan -> Lang.send(player, "info.content", buildPlaceholders(clan)),
                () -> Lang.send(player, "info.no-clan")
        );
    }

    private Map<String, String> buildPlaceholders(Clan clan) {
        ClanLevelService levelService = PrimeClans.getInstance().getLevelService();
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();
        FileConfiguration config = PrimeClans.getInstance().getConfig();

        String ownerName = Optional.of(clan.owner())
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .orElse("хз");

        int currentLevel = clan.level();
        long currentExp = clan.exp();

        long requiredExpNext = Optional.ofNullable(levelService.levels().get(currentLevel + 1))
                .map(ClanLevel::requiredExp)
                .orElse(0L);

        long expToNext = requiredExpNext > 0 ? Math.max(0, requiredExpNext - currentExp) : 0;

        String pvpKey = clan.pvpEnabled() ? "enabled" : "disabled";
        String pvpStatus = config.getString("settings.pvp-status." + pvpKey, pvpKey);

        long onlineCount = clan.members().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .count();

        String separator = config.getString("settings.members-separator", "<gray>, ");

        List<String> allNames = getMemberNames(clan);
        List<String> onlineNames = allNames.stream()
                .filter(name -> Optional.ofNullable(Bukkit.getPlayer(name)).isPresent())
                .toList();

        return Map.ofEntries(
                Map.entry("clan_name", clan.name()),
                Map.entry("clan_owner", ownerName),
                Map.entry("clan_level", String.valueOf(currentLevel)),
                Map.entry("clan_exp", String.valueOf(currentExp)),
                Map.entry("clan_exp_to_next", String.valueOf(expToNext)),
                Map.entry("clan_members_current", String.valueOf(clan.members().size())),
                Map.entry("clan_members_max", String.valueOf(clan.getMaxMembers())),
                Map.entry("clan_members_info", clan.members().size() + "/" + clan.getMaxMembers()),
                Map.entry("members_online", String.valueOf(onlineCount)),
                Map.entry("clan_balance", economy.format(clan.balance())),
                Map.entry("clan_pvp", pvpStatus),
                Map.entry("clan_members_list", String.join(separator, allNames)),
                Map.entry("clan_members_online_list", String.join(separator, onlineNames))
        );
    }

    private List<String> getMemberNames(Clan clan) {
        return clan.members().stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {return "info";}
}
package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;

@RequiredArgsConstructor
public class BalanceSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(
                clan -> sendBalanceInfo(player, clan),
                () -> Lang.send(player, "balance.no-clan")
        );
    }

    private void sendBalanceInfo(Player player, Clan clan) {
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();
        String formattedBalance = economy.format(clan.balance());

        Lang.send(player, "balance.info", Map.of(
                "clan", clan.name(),
                "balance", formattedBalance
        ));
    }

    @Override
    public String getName() {return "balance";}
}
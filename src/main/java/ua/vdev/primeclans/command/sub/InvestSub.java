package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class InvestSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(
                clan -> {
                    if (!clan.hasPerm(player.getUniqueId(), ClanPerm.INVEST_BALANCE)) {
                        Lang.send(player, "perm.no-perm");
                        return;
                    }
                    parseAmount(args).ifPresentOrElse(
                            amount -> validateAndInvest(player, amount),
                            () -> Lang.send(player, "invest.usage")
                    );
                },
                () -> Lang.send(player, "invest.no-clan")
        );
    }

    private Optional<Double> parseAmount(String[] args) {
        return Optional.ofNullable(args)
                .filter(a -> a.length >= 2)
                .map(a -> a[1])
                .flatMap(this::tryParseDouble)
                .filter(amount -> amount > 0);
    }

    private Optional<Double> tryParseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void validateAndInvest(Player player, double amount) {
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();

        if (!economy.has(player, amount)) {
            Lang.send(player, "invest.no-money", Map.of(
                    "amount", economy.format(amount),
                    "balance", economy.format(economy.getBalance(player))
            ));
            return;
        }

        clanManager.getPlayerClan(player.getUniqueId()).ifPresent(clan -> {
            economy.withdraw(player, amount);
            clanManager.deposit(clan.name(), amount);
            Lang.send(player, "invest.success", Map.of(
                    "amount", economy.format(amount),
                    "balance", economy.format(clan.balance() + amount)
            ));
        });
    }

    @Override
    public String getName() { return "invest"; }
}
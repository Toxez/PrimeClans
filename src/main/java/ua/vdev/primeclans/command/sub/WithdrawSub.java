package ua.vdev.primeclans.command.sub;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.economy.EconomyManager;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class WithdrawSub implements SubCommand {

    private final ClanManager clanManager;

    @Override
    public void execute(Player player, String[] args) {
        clanManager.getPlayerClan(player.getUniqueId()).ifPresentOrElse(
                clan -> processWithdraw(player, args, clan),
                () -> Lang.send(player, "withdraw.no-clan")
        );
    }

    private void processWithdraw(Player player, String[] args, Clan clan) {
        if (!clan.hasPerm(player.getUniqueId(), ClanPerm.WITHDRAW_BALANCE)) {
            Lang.send(player, "perm.no-perm");
            return;
        }

        parseAmount(args).ifPresentOrElse(
                amount -> validateAndWithdraw(player, amount, clan),
                () -> Lang.send(player, "withdraw.usage")
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

    private void validateAndWithdraw(Player player, double amount, Clan clan) {
        EconomyManager economy = PrimeClans.getInstance().getEconomyManager();

        if (clan.balance() < amount) {
            Lang.send(player, "withdraw.not-enough", Map.of(
                    "amount", economy.format(amount),
                    "balance", economy.format(clan.balance())
            ));
            return;
        }

        clanManager.withdraw(clan.name(), amount);
        economy.deposit(player, amount);

        Lang.send(player, "withdraw.success", Map.of(
                "amount", economy.format(amount),
                "balance", economy.format(clan.balance() - amount)
        ));
    }

    @Override
    public String getName() { return "withdraw"; }
}
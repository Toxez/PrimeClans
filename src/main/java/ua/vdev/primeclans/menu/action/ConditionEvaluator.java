package ua.vdev.primeclans.menu.action;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.api.variable.VariableRegistry;
import ua.vdev.primeclans.model.Clan;

import java.util.Optional;

public class ConditionEvaluator {

    public static boolean evaluate(Player player, String condition) {
        if (condition == null || condition.isBlank()) return false;
        Clan clan = PrimeClans.getInstance().getClanManager()
                .getPlayerClan(player.getUniqueId())
                .orElse(null);

        String parsed = VariableRegistry.replaceAll(condition, player, clan);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            parsed = PlaceholderAPI.setPlaceholders(player, parsed);
        }

        Optional<Operator> opOpt = Operator.findIn(parsed);

        if (opOpt.isEmpty()) {
            return Boolean.parseBoolean(parsed.trim());
        }

        Operator operator = opOpt.get();
        String[] parts = parsed.split(operator.getSymbol(), 2);
        if (parts.length != 2) return false;

        String left = parts[0].trim();
        String right = parts[1].trim();

        // оно тут так как ему нужно знать правую часть выражения (само право) не нужно мне писать об этом в тг пожалуйста
        if (left.equalsIgnoreCase("has_clan_perm")) {
            if (clan == null) return operator == Operator.NOT_EQUALS;
            boolean hasPerm = clan.hasPerm(player.getUniqueId(), right.toUpperCase());
            return operator == Operator.EQUALS ? hasPerm : !hasPerm;
        }

        try {
            double leftNum = Double.parseDouble(left);
            double rightNum = Double.parseDouble(right);
            return operator.evaluate(leftNum, rightNum);
        } catch (NumberFormatException e) {
            return operator.evaluate(left, right);
        }
    }
}
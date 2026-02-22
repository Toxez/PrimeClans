package ua.vdev.primeclans.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ua.vdev.primeclans.command.sub.*;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerFind;
import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final ClanManager clanManager;

    public ClanCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
        subCommands.put("create", new CreateSub(clanManager));
        subCommands.put("delete", new DeleteSub(clanManager));
        subCommands.put("invite", new InviteSub(clanManager));
        subCommands.put("accept", new AcceptSub(clanManager));
        subCommands.put("kick", new KickSub(clanManager));
        subCommands.put("leave", new LeaveSub(clanManager));
        subCommands.put("setleader", new SetLeaderSub(clanManager));
        subCommands.put("chat", new ChatSub(clanManager));
        subCommands.put("menu", new MenuSub(clanManager));
        subCommands.put("pvp", new PvpSub(clanManager));
        subCommands.put("balance", new BalanceSub(clanManager));
        subCommands.put("invest", new InvestSub(clanManager));
        subCommands.put("withdraw", new WithdrawSub(clanManager));
        subCommands.put("info", new InfoSub(clanManager));
        subCommands.put("glow", new GlowSub(clanManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            String subName = (args.length > 0) ? args[0].toLowerCase() : "";
            boolean inClan = clanManager.getPlayerClan(player.getUniqueId()).isPresent();
            if (subName.equals("create") && inClan) {
                Lang.send(player, "create.already-in-clan");
                return true;
            }

            Optional.ofNullable(subCommands.get(subName))
                    .ifPresentOrElse(
                            sub -> sub.execute(player, args),
                            () -> showHelp(player, inClan)
                    );
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        String input = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        boolean inClan = clanManager.getPlayerClan(player.getUniqueId()).isPresent();
        if (args.length == 1) {
            return getAvailableSubCommands(inClan, input);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "invite" -> inClan ? suggestOnlinePlayers(player, input) : List.of();
                case "kick", "setleader" -> inClan ? suggestClanMembers(player, input) : List.of();
                case "accept" -> !inClan ? suggestClanNames(input) : List.of();
                default -> List.of();
            };
        }

        return Collections.emptyList();
    }

    private List<String> getAvailableSubCommands(boolean inClan, String input) {
        Set<String> allowed = inClan
                ? Set.of("delete", "invite", "kick", "leave", "setleader", "chat", "menu", "pvp", "balance", "invest", "withdraw", "info", "glow")
                : Set.of("create", "accept");
        return allowed.stream()
                .filter(name -> name.startsWith(input))
                .sorted()
                .toList();
    }

    private List<String> suggestOnlinePlayers(Player sender, String input) {
        return PlayerFind.all()
                .except(sender)
                .matching(p -> p.getName().toLowerCase().startsWith(input))
                .asNames()
                .stream()
                .sorted()
                .toList();
    }

    private List<String> suggestClanMembers(Player sender, String input) {
        return clanManager.getPlayerClan(sender.getUniqueId())
                .map(clan -> clan.members().stream()
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull)
                        .filter(name -> !name.equalsIgnoreCase(sender.getName()))
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .sorted()
                        .toList())
                .orElse(List.of());
    }

    private List<String> suggestClanNames(String input) {
        return clanManager.getClanNames().stream()
                .filter(name -> name.toLowerCase().startsWith(input))
                .sorted()
                .toList();
    }

    private void showHelp(Player player, boolean inClan) {
        if (inClan) {
            clanManager.getPlayerClan(player.getUniqueId()).ifPresent(clan ->
                    Lang.send(player, "help.in-clan", Map.of("clan", clan.name())));
        } else {
            Lang.send(player, "help.no-clan");
        }
    }
}
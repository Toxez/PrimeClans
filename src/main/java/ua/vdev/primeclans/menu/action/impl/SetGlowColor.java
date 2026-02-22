package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.glow.GlowColor;
import ua.vdev.primeclans.manager.ClanManager;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.model.Clan;
import ua.vdev.primeclans.perm.ClanPerm;
import ua.vdev.primeclans.util.Lang;
import ua.vdev.vlibapi.player.PlayerMsg;

import java.util.Optional;

public class SetGlowColor implements MenuAction {
    private final String colorInput;
    public SetGlowColor(String colorInput) {
        this.colorInput = colorInput;
    }

    @Override
    public void execute(Player player) {
        ClanManager cm = PrimeClans.getInstance().getClanManager();
        cm.getPlayerClan(player.getUniqueId())
                .ifPresentOrElse(
                        clan -> processAction(player, clan, cm),
                        () -> Lang.send(player, "glow.no-clan")
                );
    }

    private void processAction(Player player, Clan clan, ClanManager cm) {
        if (!clan.hasPerm(player.getUniqueId(), ClanPerm.MANAGE_GLOW)) {
            Lang.send(player, "perm.no-perm");
            return;
        }

        parseColor().ifPresentOrElse(
                color -> {
                    cm.setGlowColor(clan.name(), color);
                    player.updateInventory();
                },
                () -> PlayerMsg.send(player, "<red>Неправильный формат цвета - <gold>" + colorInput)
        );
    }

    private Optional<GlowColor> parseColor() {
        return Optional.ofNullable(colorInput)
                .filter(input -> !input.isBlank())
                .flatMap(input -> {
                    if (input.startsWith("#")) {
                        return GlowColor.fromHex(input);
                    }
                    return parseRgb(input);
                });
    }

    private Optional<GlowColor> parseRgb(String input) {
        return Optional.of(input.split(";"))
                .filter(parts -> parts.length == 3)
                .flatMap(parts -> {
                    try {
                        int r = Integer.parseInt(parts[0].trim());
                        int g = Integer.parseInt(parts[1].trim());
                        int b = Integer.parseInt(parts[2].trim());
                        return Optional.of(GlowColor.of(r, g, b));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                });
    }
}
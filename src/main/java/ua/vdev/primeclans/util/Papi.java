package ua.vdev.primeclans.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.vdev.primeclans.PrimeClans;

public class Papi extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "primeclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Tox_8729";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("name")) {
            return PrimeClans.getInstance().getClanManager()
                    .getPlayerClan(player.getUniqueId())
                    .map(clan -> clan.name())
                    .orElse("");
        }

        return null;
    }
}
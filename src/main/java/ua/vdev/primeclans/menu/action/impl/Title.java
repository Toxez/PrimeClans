package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.vlibapi.util.TextColor;

import java.time.Duration;
import java.util.Optional;

public class Title implements MenuAction {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void execute(Player player) {
        net.kyori.adventure.title.Title.Times times = net.kyori.adventure.title.Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L));
        net.kyori.adventure.title.Title titleObj = net.kyori.adventure.title.Title.title(
                TextColor.parse(Optional.ofNullable(title).orElse("")),
                TextColor.parse(Optional.ofNullable(subtitle).orElse("")), times);

        player.showTitle(titleObj);
    }
}
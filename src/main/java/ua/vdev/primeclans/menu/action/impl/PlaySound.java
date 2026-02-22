package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.MenuAction;

public class PlaySound implements MenuAction {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public PlaySound(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void execute(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
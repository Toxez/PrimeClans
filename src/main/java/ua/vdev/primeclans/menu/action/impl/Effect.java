package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ua.vdev.primeclans.menu.action.MenuAction;

import java.util.Optional;

public class Effect implements MenuAction {
    private final PotionEffectType effectType;
    private final int duration;
    private final int amplifier;

    public Effect(PotionEffectType effectType, int duration, int amplifier) {
        this.effectType = effectType;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public void execute(Player player) {
        Optional.ofNullable(effectType)
                .map(type -> new PotionEffect(type, duration, amplifier))
                .ifPresent(player::addPotionEffect);
    }
}
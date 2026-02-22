package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.vlibapi.player.PlayerMsg;
import ua.vdev.vlibapi.util.scheduler.Task;

import java.util.concurrent.atomic.AtomicInteger;

public class ActionBar implements MenuAction {
    private final String message;
    private final int durationTicks;

    public ActionBar(String message, int durationTicks) {
        this.message = message;
        this.durationTicks = Math.max(20, durationTicks);
    }

    @Override
    public void execute(Player player) {
        if (message == null || message.isEmpty()) return;
        AtomicInteger counter = new AtomicInteger(0);
        int maxRepeats = durationTicks / 20;
        Task.timerSelf(0L, 20L, task -> {
            if (!player.isOnline() || counter.incrementAndGet() > maxRepeats) {
                task.cancel();
                return;
            }

            PlayerMsg.action(player, message);
        });
    }
}
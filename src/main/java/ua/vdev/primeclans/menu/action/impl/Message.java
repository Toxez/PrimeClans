package ua.vdev.primeclans.menu.action.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.vlibapi.util.TextColor;

import java.util.Map;

public class Message implements MenuAction {
    private final Component message;

    public Message(String rawText, Map<String, String> placeholders) {
        String replaced = rawText;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        this.message = TextColor.parse(replaced).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    @Override
    public void execute(Player player) {
        player.sendMessage(message);
    }
}
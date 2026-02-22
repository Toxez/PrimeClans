package ua.vdev.primeclans.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.vlibapi.player.PlayerMsg;

import java.util.Collections;
import java.util.Map;

@UtilityClass
public class Lang {

    public void send(Player player, String key, Map<String, String> placeholders) {
        PlayerMsg.lang(player, PrimeClans.getInstance().getTranslation(), key, placeholders);
    }

    public void send(Player player, String key) {
        send(player, key, Collections.emptyMap());
    }
}
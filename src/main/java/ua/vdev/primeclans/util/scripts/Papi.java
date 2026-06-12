package ua.vdev.primeclans.util.scripts;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.model.Clan;

import java.util.Optional;

public class Papi extends PlaceholderExpansion {

    private final PapiConfig config = new PapiConfig();
    @Override public @NotNull String getIdentifier() { return "primeclans"; }
    @Override public @NotNull String getAuthor() { return "Tox_8729"; }
    @Override public @NotNull String getVersion() { return "1.0"; }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        Optional<Clan> clan = PrimeClans.getInstance()
                .getClanManager()
                .getPlayerClan(player.getUniqueId());

        boolean inClan = clan.isPresent();
        String lowerParams = params.toLowerCase();

        switch (lowerParams) {
            case "name" -> {
                return inClan ? clan.get().name() : config.getNoClanFallback("name");
            }
            case "level" -> {
                return inClan ? String.valueOf(clan.get().level()) : config.getNoClanFallback("level");
            }
            case "people" -> {
                return inClan ? String.valueOf(clan.get().members().size()) : config.getNoClanFallback("people");
            }
            case "balance" -> {
                return inClan ? format(clan.get().balance()) : config.getNoClanFallback("balance");
            }
            case "exp" -> {
                return inClan ? String.valueOf(clan.get().exp()) : config.getNoClanFallback("exp");
            }
            case "exp_to_next" -> {
                if (!inClan) return config.getNoClanFallback("exp_to_next");
                Clan c = clan.get();
                long nextRequired = PrimeClans.getInstance()
                        .getLevelService()
                        .requiredExpForLevel(c.level() + 1);

                if (nextRequired == 0) return "0";
                long remaining = Math.max(0, nextRequired - c.exp());
                return String.valueOf(remaining);
            }
        }

        Script script = config.getScript(lowerParams);
        if (script == null) return null;

        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();

            Object jsPlayer = Context.javaToJS(player, scope);
            Object jsClan = Context.javaToJS(clan.orElse(null), scope);
            Object jsApi = Context.javaToJS(PrimeClans.getInstance(), scope);

            ScriptableObject.putProperty(scope, "player", jsPlayer);
            ScriptableObject.putProperty(scope, "clan", jsClan);
            ScriptableObject.putProperty(scope, "api", jsApi);

            Object result = script.exec(cx, scope);
            return result != null ? Context.toString(result) : "";

        } catch (Exception e) {
            return "§c[js ошибкв " + params + "]";
        } finally {
            Context.exit();
        }
    }

    private String format(double value) {
        return value == Math.floor(value)
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }
}
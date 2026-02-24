package ua.vdev.primeclans.api.requirement;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.PrimeClans;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.primeclans.model.Clan;
import java.util.List;
import java.util.Map;

public class RequirementGuardAction implements MenuAction {

    public enum Mode { ALL, ANY }

    private final List<RequirementCheck> checks;
    private final Mode mode;
    private final List<MenuAction> realActions;
    private final List<MenuAction> denyActions;

    public RequirementGuardAction(
            List<RequirementCheck> checks,
            Mode mode,
            List<MenuAction> realActions,
            List<MenuAction> denyActions
    ) {
        this.checks = checks;
        this.mode = mode;
        this.realActions = realActions;
        this.denyActions = denyActions;
    }

    @Override
    public void execute(Player player) {
        Clan clan = PrimeClans.getInstance().getClanManager()
                .getPlayerClan(player.getUniqueId())
                .orElse(null);

        boolean passed = mode == Mode.ALL
                ? checks.stream().allMatch(c -> c.check(player, clan))
                : checks.stream().anyMatch(c -> c.check(player, clan));

        List<MenuAction> toExecute = passed ? realActions : denyActions;
        toExecute.forEach(a -> a.execute(player));
    }

    public record RequirementCheck(AddonRequirement requirement, Map<String, Object> params) {
        public boolean check(Player player, Clan clan) {
            try {
                return requirement.check(player, clan, params);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
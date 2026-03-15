package ua.vdev.primeclans.menu.action.impl;

import org.bukkit.entity.Player;
import ua.vdev.primeclans.menu.action.ConditionEvaluator;
import ua.vdev.primeclans.menu.action.MenuAction;

import java.util.List;

public class IfAction implements MenuAction {

    private final String condition;
    private final List<MenuAction> thenActions;
    private final List<MenuAction> elseActions;

    public IfAction(String condition, List<MenuAction> thenActions, List<MenuAction> elseActions) {
        this.condition = condition;
        this.thenActions = thenActions;
        this.elseActions = elseActions;
    }

    @Override
    public void execute(Player player) {
        if (ConditionEvaluator.evaluate(player, condition)) {
            thenActions.forEach(action -> action.execute(player));
        } else {
            elseActions.forEach(action -> action.execute(player));
        }
    }
}
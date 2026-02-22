package ua.vdev.primeclans.menu.helper;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import ua.vdev.primeclans.menu.action.ActionFactory;
import ua.vdev.primeclans.menu.action.MenuAction;
import ua.vdev.vlibapi.item.ItemBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MenuHelper {

    public static Set<Integer> parseSlots(Map<?, ?> itemMap, int inventorySize) {
        return Stream.of(
                        parseSingleSlot(itemMap),
                        parseMultipleSlots(itemMap)
                )
                .flatMap(Set::stream)
                .filter(slot -> slot >= 0 && slot < inventorySize)
                .collect(Collectors.toSet());
    }

    private static Set<Integer> parseSingleSlot(Map<?, ?> itemMap) {
        return Optional.ofNullable(itemMap.get("slot"))
                .filter(Number.class::isInstance)
                .map(n -> ((Number) n).intValue())
                .map(Set::of)
                .orElse(Collections.emptySet());
    }

    private static Set<Integer> parseMultipleSlots(Map<?, ?> itemMap) {
        return Optional.ofNullable(itemMap.get("slots"))
                .filter(List.class::isInstance)
                .map(l -> (List<?>) l)
                .map(list -> list.stream()
                        .filter(Number.class::isInstance)
                        .map(n -> ((Number) n).intValue())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public static Optional<List<MenuAction>> parseActions(Map<?, ?> itemMap, String actionKey, Map<String, Object> context) {
        return Optional.ofNullable(itemMap.get(actionKey))
                .filter(List.class::isInstance)
                .map(obj -> (List<?>) obj)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .filter(s -> !s.isEmpty())
                        .toList())
                .map(actionStrings -> ActionFactory.create(actionStrings, context));
    }

    public static void loadMenuItems(Inventory inventory, ConfigurationSection menuSection, Map<String, String> placeholders, Map<String, Object> actionContext, Map<Integer, List<MenuAction>> leftActions, Map<Integer, List<MenuAction>> rightActions) {
        Optional.ofNullable(menuSection.getList("items", Collections.emptyList()))
                .stream()
                .flatMap(List::stream)
                .filter(obj -> obj instanceof Map<?, ?>)
                .map(obj -> (Map<?, ?>) obj)
                .forEach(itemMap -> processMenuItem(
                        inventory,
                        itemMap,
                        placeholders,
                        actionContext,
                        leftActions,
                        rightActions
                ));
    }

    private static void processMenuItem(Inventory inventory, Map<?, ?> itemMap, Map<String, String> placeholders, Map<String, Object> actionContext, Map<Integer, List<MenuAction>> leftActions, Map<Integer, List<MenuAction>> rightActions) {
        Optional.ofNullable(ItemBuilder.fromMap(itemMap, placeholders))
                .ifPresent(item -> {
                    Set<Integer> slots = parseSlots(itemMap, inventory.getSize());
                    slots.forEach(slot -> inventory.setItem(slot, item.clone()));
                    parseActions(itemMap, "left_click_actions", actionContext)
                            .ifPresent(actions -> slots.forEach(slot -> leftActions.put(slot, actions)));
                    parseActions(itemMap, "right_click_actions", actionContext)
                            .ifPresent(actions -> slots.forEach(slot -> rightActions.put(slot, actions)));
                });
    }
}
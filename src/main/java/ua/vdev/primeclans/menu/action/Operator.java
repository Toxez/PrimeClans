package ua.vdev.primeclans.menu.action;

import java.util.Arrays;
import java.util.Optional;

public enum Operator {
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_OR_EQUALS(">="),
    LESS_OR_EQUALS("<="),
    GREATER(">"),
    LESS("<");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Optional<Operator> findIn(String text) {
        return Arrays.stream(values())
                .filter(op -> text.contains(op.getSymbol()))
                .findFirst();
    }

    public boolean evaluate(double left, double right) {
        return switch (this) {
            case EQUALS -> left == right;
            case NOT_EQUALS -> left != right;
            case GREATER_OR_EQUALS -> left >= right;
            case LESS_OR_EQUALS -> left <= right;
            case GREATER -> left > right;
            case LESS -> left < right;
        };
    }

    public boolean evaluate(String left, String right) {
        return switch (this) {
            case EQUALS -> left.equalsIgnoreCase(right);
            case NOT_EQUALS -> !left.equalsIgnoreCase(right);
            default -> false;
        };
    }
}
package ua.vdev.primeclans.glow;

import org.bukkit.Color;

import java.util.Objects;
import java.util.Optional;

public final class GlowColor {
    private final int r, g, b;

    public GlowColor(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    public static GlowColor of(int r, int g, int b) {
        return new GlowColor(r, g, b);
    }

    public static Optional<GlowColor> fromHex(String hex) {
        return Optional.ofNullable(hex)
                .filter(s -> !s.isBlank())
                .map(s -> s.replace("#", "").trim())
                .filter(s -> s.length() == 6)
                .flatMap(GlowColor::parseHex);
    }

    private static Optional<GlowColor> parseHex(String clean) {
        try {
            int r = Integer.parseInt(clean.substring(0, 2), 16);
            int g = Integer.parseInt(clean.substring(2, 4), 16);
            int b = Integer.parseInt(clean.substring(4, 6), 16);
            return Optional.of(new GlowColor(r, g, b));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public Color asBukkit() {
        return Color.fromRGB(r, g, b);
    }

    public String toHex() {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public int r() { return r; }
    public int g() { return g; }
    public int b() { return b; }

    @Override
    public boolean equals(Object o) {
        return Optional.ofNullable(o)
                .filter(obj -> this == obj || obj instanceof GlowColor other
                        && r == other.r && g == other.g && b == other.b)
                .isPresent();
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b);
    }

    @Override
    public String toString() {
        return toHex();
    }
}

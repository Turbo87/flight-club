package org.flightclub.compat;

public class FontFactory {

    public enum Style {
        PLAIN,
        BOLD,
        ITALIC,
    }

    public static Font create(String name, Style style, int size) {
        return new AwtFont(name, style.ordinal(), size);
    }

    private FontFactory() {}
}

package org.flightclub.compat;

import org.flightclub.Platform;

public class FontFactory {

    public enum Style {
        PLAIN,
        BOLD,
        ITALIC,
    }

    public static Font create(String name, Style style, int size) {
        if (Platform.isAndroid())
            return new AndroidFont(name, style, size);
        else
            return new AwtFont(name, style.ordinal(), size);
    }

    private FontFactory() {}
}

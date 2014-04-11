package org.flightclub.graphics;

import org.flightclub.Platform;
import org.flightclub.graphics.android.AndroidFont;
import org.flightclub.graphics.awt.AwtFont;

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

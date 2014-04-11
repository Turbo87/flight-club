package org.flightclub.compat;

import org.flightclub.Platform;

public class ColorFactory {
    public final static Color WHITE = create(255, 255, 255);
    public final static Color LIGHT_GRAY = create(192, 192, 192);
    public final static Color GRAY = create(128, 128, 128);
    public final static Color DARK_GRAY = create(64, 64, 64);
    public final static Color BLACK = create(0, 0, 0);
    public final static Color RED = create(255, 0, 0);
    public final static Color PINK = create(255, 175, 175);
    public final static Color ORANGE = create(255, 200, 0);
    public final static Color YELLOW = create(255, 255, 0);
    public final static Color GREEN = create(0, 255, 0);
    public final static Color MAGENTA = create(255, 0, 255);
    public final static Color CYAN = create(0, 255, 255);
    public final static Color BLUE = create(0, 0, 255);

    public static Color create(int red, int green, int blue) {
        if (Platform.isAndroid())
            return new AndroidColor(red, green, blue);
        else
            return new AwtColor(red, green, blue);
    }

    private ColorFactory() {}
}

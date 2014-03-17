package org.flightclub.compat;

public class Color {
    private final java.awt.Color c;


    /**
     * The color white.  In the default sRGB space.
     */
    public final static Color WHITE = new Color(255, 255, 255);

    /**
     * The color light gray.  In the default sRGB space.
     */
    public final static Color LIGHT_GRAY = new Color(192, 192, 192);

    /**
     * The color gray.  In the default sRGB space.
     */
    public final static Color GRAY = new Color(128, 128, 128);

    /**
     * The color dark gray.  In the default sRGB space.
     */
    public final static Color DARK_GRAY = new Color(64, 64, 64);

    /**
     * The color black.  In the default sRGB space.
     */
    public final static Color BLACK = new Color(0, 0, 0);

    /**
     * The color red.  In the default sRGB space.
     */
    public final static Color RED = new Color(255, 0, 0);

    /**
     * The color pink.  In the default sRGB space.
     */
    public final static Color PINK = new Color(255, 175, 175);

    /**
     * The color orange.  In the default sRGB space.
     */
    public final static Color ORANGE = new Color(255, 200, 0);

    /**
     * The color yellow.  In the default sRGB space.
     */
    public final static Color YELLOW = new Color(255, 255, 0);

    /**
     * The color green.  In the default sRGB space.
     */
    public final static Color GREEN = new Color(0, 255, 0);

    /**
     * The color magenta.  In the default sRGB space.
     */
    public final static Color MAGENTA = new Color(255, 0, 255);

    /**
     * The color cyan.  In the default sRGB space.
     */
    public final static Color CYAN = new Color(0, 255, 255);

    /**
     * The color blue.  In the default sRGB space.
     */
    public final static Color BLUE = new Color(0, 0, 255);

    public Color(int r, int g, int b) {
        this.c = new java.awt.Color(r, g, b);
    }

    public java.awt.Color getColor() {
        return c;
    }

    public int getRed() {
        return c.getRed();
    }

    public int getGreen() {
        return c.getGreen();
    }

    public int getBlue() {
        return c.getBlue();
    }
}

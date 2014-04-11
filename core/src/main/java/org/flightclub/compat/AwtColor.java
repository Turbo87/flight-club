package org.flightclub.compat;

public class AwtColor implements Color {
    private final java.awt.Color c;

    public AwtColor(int r, int g, int b) {
        this.c = new java.awt.Color(r, g, b);
    }

    public java.awt.Color getRaw() {
        return c;
    }

    @Override
    public int getRed() {
        return c.getRed();
    }

    @Override
    public int getGreen() {
        return c.getGreen();
    }

    @Override
    public int getBlue() {
        return c.getBlue();
    }
}

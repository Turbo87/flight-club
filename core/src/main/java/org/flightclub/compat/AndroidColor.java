package org.flightclub.compat;

public class AndroidColor implements Color {
    private final int r;
    private final int g;
    private final int b;

    public AndroidColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public int getRed() {
        return r;
    }

    @Override
    public int getGreen() {
        return g;
    }

    @Override
    public int getBlue() {
        return b;
    }
}

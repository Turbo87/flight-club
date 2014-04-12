package org.flightclub;

public class MouseTracker {
    private boolean dragging = false;
    private int x0 = 0, y0 = 0;
    private int dx = 0, dy = 0;

    public void pressed(int x, int y) {
        x0 = x;
        y0 = y;

        dx = 0;
        dy = 0;

        dragging = true;
    }

    public void released() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public void dragged(int x, int y) {
        dx = x - x0;
        dy = y - y0;
    }
}

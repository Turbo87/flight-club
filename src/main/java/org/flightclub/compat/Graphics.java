package org.flightclub.compat;

import java.awt.Color;
import java.awt.Font;

public class Graphics {

    private final java.awt.Graphics g;

    public Graphics(java.awt.Graphics g) {
        this.g = g;
    }

    public void setColor(Color color) {
        g.setColor(color);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    public void setFont(Font font) {
        g.setFont(font);
    }

    public void drawString(String str, int x, int y) {
        g.drawString(str, x, y);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.fillPolygon(xPoints, yPoints, nPoints);
    }
}

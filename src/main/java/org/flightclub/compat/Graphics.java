package org.flightclub.compat;

public class Graphics {

    private final java.awt.Graphics g;

    public Graphics(java.awt.Graphics g) {
        this.g = g;
    }

    public void setColor(Color color) {
        g.setColor(color.getColor());
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    public void setFont(Font font) {
        g.setFont(font.getFont());
    }

    public void drawString(String str, int x, int y) {
        g.drawString(str, x, y);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillOval(int x, int y, int width, int height) {
        g.fillOval(x, y, width, height);
    }
}

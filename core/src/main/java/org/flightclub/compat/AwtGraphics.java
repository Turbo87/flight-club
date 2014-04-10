package org.flightclub.compat;

public class AwtGraphics implements Graphics {

    private final java.awt.Graphics g;

    public AwtGraphics(java.awt.Graphics g) {
        this.g = g;
    }

    @Override
    public void setColor(Color color) {
        g.setColor(color.getColor());
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void setFont(Font font) {
        g.setFont(font.getFont());
    }

    @Override
    public void drawString(String str, int x, int y) {
        g.drawString(str, x, y);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        g.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillCircle(int x, int y, int diameter) {
        g.fillOval(x, y, diameter, diameter);
    }
}

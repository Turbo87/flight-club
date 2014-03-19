package org.flightclub.compat;

public interface Graphics {
    void setColor(Color color);
    void setFont(Font font);

    void drawLine(int x1, int y1, int x2, int y2);
    void drawString(String str, int x, int y);

    void fillOval(int x, int y, int width, int height);
    void fillPolygon(int[] xPoints, int[] yPoints, int nPoints);
}

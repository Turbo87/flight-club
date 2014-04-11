package org.flightclub.graphics.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import org.flightclub.graphics.Color;
import org.flightclub.graphics.Font;
import org.flightclub.graphics.Graphics;

public class AndroidGraphics implements Graphics {
    private final Paint paint;
    private Canvas canvas;

    public AndroidGraphics() {
        this.paint = new Paint();
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void setColor(Color color) {
        paint.setColor(0xFF000000 | color.getRed() << 16 | color.getGreen() << 8 | color.getBlue());
    }

    @Override
    public void setFont(Font font) {
        AndroidFont f = (AndroidFont) font;
        paint.setTypeface(Typeface.create(f.getName(), f.getStyle().ordinal()));
        paint.setTextSize(f.getSize());
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    @Override
    public void drawString(String str, int x, int y) {
        canvas.drawText(str, x, y, paint);
    }

    @Override
    public void fillCircle(int x, int y, int diameter) {
        canvas.drawCircle(x, y, diameter / 2, paint);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path path = new Path();
        for (int i = 0; i < nPoints; i++) {
            int x = xPoints[i];
            int y = yPoints[i];

            if (i == 0)
                path.moveTo(x, y);
            else
                path.lineTo(x, y);
        }
        path.close();

        canvas.drawPath(path, paint);
    }
}
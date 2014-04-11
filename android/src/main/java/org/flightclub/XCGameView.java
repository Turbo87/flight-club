package org.flightclub;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import org.flightclub.graphics.android.AndroidGraphics;

public class XCGameView extends View {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 720;

    private final XCGame app = new XCGame();
    private final AndroidGraphics graphics = new AndroidGraphics();

    public XCGameView(Context context) {
        this(context, null);
    }

    public XCGameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XCGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        app.init(new Interface() {
            @Override
            public int getWidth() {
                return WIDTH;
            }

            @Override
            public int getHeight() {
                return HEIGHT;
            }

            @Override
            public void play(String s) {

            }
        });
        app.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        graphics.setCanvas(canvas);
        app.draw(graphics, WIDTH, HEIGHT);
        invalidate();
    }
}

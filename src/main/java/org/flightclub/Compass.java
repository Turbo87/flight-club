/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Font;
import org.flightclub.compat.Graphics;

/**
 * a simple compass
 */
public class Compass {
    // head of arrow
    final int[] hxs = {0, 2, -2};
    final int[] hys = {5, 2, 2};

    // tail
    final int[] txs = {0, 0};
    final int[] tys = {1, -5};

    // rotate and translate above points
    final int[] hxs_ = new int[3];
    final int[] hys_ = new int[3];
    final int[] txs_ = new int[2];
    final int[] tys_ = new int[2];

    final int r;
    final int x0;
    final int y0;

    // vector determines arrow direction - start pointing north
    private float vx = 0;
    private float vy = 1;

    final float[][] m = new float[2][2];
    final Color color = Color.LIGHT_GRAY;
    final Color color2 = Color.GRAY;

    // default radius of 10
    static final int SIZE_DEFAULT = 20;

    static final int H_NUM = 3;
    static final int T_NUM = 2;

    // pixel space for label at bottom
    static final int dy = 10;

    public Compass(int inSize, int inX0, int inY0) {
        r = inSize / 2;
        x0 = inX0;
        y0 = inY0;
        init();
    }

    public Compass() {
        this(SIZE_DEFAULT, 30, 42);
    }

    void init() {
        /*
         * scale arrow head and tail to size
         * also flip y coord (screen +y points down)
         */
        float s = (float) (r - 2) / 5;

        for (int i = 0; i < H_NUM; i++) {
            hxs[i] = (int) (hxs[i] * s);
            hys[i] = (int) (hys[i] * -s);
        }

        for (int i = 0; i < T_NUM; i++) {
            txs[i] = (int) (txs[i] * s);
            tys[i] = (int) (tys[i] * -s);
        }
        updateArrow();
    }

    void setArrow(float x, float y) {
        // +y is north
        // +x is east
        vx = x;
        vy = y;

        // normalize
        float d = (float) Math.sqrt(x * x + y * y);
        vx = vx / d;
        vy = vy / d;

        // calc arrow points
        updateArrow();
    }

    public void draw(Graphics g) {

        g.setColor(color);
        g.drawLine(txs_[0], tys_[0], txs_[1], tys_[1]);

        Font font = new Font("SansSerif", Font.PLAIN, 10);
        g.setFont(font);
        g.setColor(color);
        g.drawString("N", x0 - 3, y0 - r * 2 - dy);
        g.drawString("S", x0 - 3, y0);

        g.setColor(color2);
        g.fillPolygon(hxs_, hys_, hxs_.length);
    }

    void updateArrow() {
        // rotate
        m[0][0] = vy;
        m[0][1] = -vx;
        m[1][0] = -m[0][1];
        m[1][1] = m[0][0];

        // transform
        for (int i = 0; i < H_NUM; i++) {
            hxs_[i] = (int) (m[0][0] * hxs[i] + m[0][1] * hys[i] + x0);
            hys_[i] = (int) (m[1][0] * hxs[i] + m[1][1] * hys[i] + y0 - dy - r);
        }

        for (int i = 0; i < T_NUM; i++) {
            txs_[i] = (int) (m[0][0] * txs[i] + m[0][1] * tys[i] + x0);
            tys_[i] = (int) (m[1][0] * txs[i] + m[1][1] * tys[i] + y0 - dy - r);
        }
    }

}
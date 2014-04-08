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
 * a dot on a line - use for eg vario
 * minimal design - cf toshiba scan of fred
 */
public class DataSlider {
    /** length of slider in pixels */
    final int size;

    // screen coords of center point of slider
    final int x0;
    final int y0;

    // value to display
    private float v;

    // screen coords of v (v_min = 0, v_max = size)
    int v_;

    final float v_min;
    final float v_max;

    String label = null;

    final Color color = Color.LIGHT_GRAY;
    final Color color2 = Color.GRAY;

    // default radius of 10
    static final int SIZE_DEFAULT = 20;

    // pixel space for label at bottom
    static final int dx = 2;
    static final int dy = 10;

    public DataSlider(float inV_min, float inV_max, int inSize, int inX0, int inY0) {
        v_min = inV_min;
        v_max = inV_max;
        size = inSize;
        x0 = inX0;
        y0 = inY0;
        init();
    }

    public DataSlider() {
        this(-1, 1, SIZE_DEFAULT, 50, 42);
    }

    void init() {
        // need this ?
        setValue((v_min + v_max) / 2);
    }

    void setValue(float inV) {
        // clamp value and convert to 'screen' coords
        v = inV;
        if (v <= v_min) v = v_min;
        if (inV >= v_max) v = v_max;

        v = inV;
        v_ = (int) (((v - v_min) / (v_max - v_min)) * size);
    }

    public void draw(Graphics g) {

        g.setColor(color);
        g.drawLine(x0 - dx, y0 - size - dy, x0 + dx, y0 - size - dy);
        g.drawLine(x0 - dx, y0 - dy, x0 + dx, y0 - dy);
        g.drawLine(x0, y0 - dy, x0, y0 - size - dy);

        if (label != null) {
            Font font = new Font("SansSerif", Font.PLAIN, 10);
            g.setFont(font);
            g.drawString(label, x0 - 10, y0);
        }

        g.setColor(color2);
        g.fillCircle(x0 - 1, y0 - v_ - dy - 1, 3);
    }
}
/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.graphics.Color;
import org.flightclub.graphics.ColorFactory;

import java.util.Vector;

public class GliderShape extends Object3dWithShadow {
    public static final Color DEFAULT_COLOR = ColorFactory.create(170, 170, 180);
    public static final float HEIGHT = (float) 0.2;

    final Color color;

    /** default constructor - gray shape */
    public GliderShape(XCGame app) {
        this(app, DEFAULT_COLOR);
    }

    /* pass in a color (eg for user glider) */
    public GliderShape(XCGame app, Color color) {
        super(app, false);
        this.color = color;
        init();
    }

    private void init() {
        Vector<Vector3d> wire;

        // chord
        float y = (float) 0.2;
        // nose a bit up
        float z = y * (float) 0.3;
        // anhedral
        float a = (float) 0.15;
        // sweep
        float s = (float) 0.4;

        wire = new Vector<>();
        wire.addElement(new Vector3d(0, y, z));
        wire.addElement(new Vector3d(1, y - s, z + a));
        wire.addElement(new Vector3d(1, -s, a));
        wire.addElement(new Vector3d(0, 0, 0));
        wire.addElement(new Vector3d(0, y, z));
        addWire(wire, color, true, true);

        wire = new Vector<>();
        wire.addElement(new Vector3d(0, y, z));
        wire.addElement(new Vector3d(0, 0, 0));
        wire.addElement(new Vector3d(-1, -s, a));
        wire.addElement(new Vector3d(-1, y - s, z + a));
        wire.addElement(new Vector3d(0, y, z));
        addWire(wire, color, true, true);

        // crazy bug - shadow cocks up tips !
        // same points but reverse order for underside
        wire = new Vector<>();
        wire.addElement(new Vector3d(0, y, z));
        wire.addElement(new Vector3d(-1, y - s, z + a));
        wire.addElement(new Vector3d(-1, -s, a));
        wire.addElement(new Vector3d(0, 0, 0));
        wire.addElement(new Vector3d(0, y, z));
        addWireWithShadow(wire, color, true, true);

        wire = new Vector<>();
        wire.addElement(new Vector3d(0, y, z));
        wire.addElement(new Vector3d(0, 0, 0));
        wire.addElement(new Vector3d(1, -s, a));
        wire.addElement(new Vector3d(1, y - s, z + a));
        wire.addElement(new Vector3d(0, y, z));
        addWireWithShadow(wire, color, true, true);

        scaleBy(HEIGHT);
    }
}


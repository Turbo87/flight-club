/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;

import java.util.Vector;

public class GliderShape2 extends Object3dWithShadow {
    final Color color;
    final static float height = (float) 0.2;//0.2 for shape 1

    /** default constructor - gray shape */
    public GliderShape2(XCGame app, boolean register) {
        super(app, register);
        color = new Color(170, 170, 180); //180
        init();
    }

    /* pass in a color (eg for user glider) */
    public GliderShape2(XCGame app, boolean register, Color inC) {
        super(app, register);
        color = inC;
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

        scaleBy(height);
    }
}


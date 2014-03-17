/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.*;

public class Surface extends PolyLine {

    final int[] xs;
    final int[] ys;

    public Surface(Object3d o, int inNumPoints, Color inC) {
        super(o, inNumPoints, inC);

        isSolid = true;

        xs = new int[numPoints];
        ys = new int[numPoints];
    }

    public void draw(Graphics g) {
        Vector3d a;
        Vector3d b;
        boolean ok = true;

        if (numPoints <= 1) return;

        g.setColor(super.getColor());

        for (int i = 0; i < numPoints; i++) {
            a = object3d.points_.elementAt(points[i]);
            Boolean inFOV = object3d.inFOVs.elementAt(points[i]);
            ok = ok && inFOV;
            xs[i] = (int) (a.y);
            ys[i] = (int) (a.z);
        }
        if (ok) g.fillPolygon(xs, ys, xs.length);
    }

}

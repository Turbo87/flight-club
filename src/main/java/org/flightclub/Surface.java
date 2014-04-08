/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Graphics;

public class Surface extends PolyLine {

    final int[] xs;
    final int[] ys;

    public Surface(Object3d object, int numPoints, Color color) {
        super(object, numPoints, color);

        isSolid = true;

        xs = new int[numPoints];
        ys = new int[numPoints];
    }

    @Override
    public void draw(Graphics g) {
        Vector3d a;
        Vector3d b;
        boolean inFOV = true;

        if (numPoints <= 1) return;

        g.setColor(getColor());

        for (int i = 0; i < numPoints; i++) {
            a = object3d.points_.elementAt(points[i]);
            inFOV = inFOV && object3d.inFOVs.elementAt(points[i]);
            xs[i] = (int) (a.y);
            ys[i] = (int) (a.z);
        }

        if (inFOV)
            g.fillPolygon(xs, ys, xs.length);
    }

}

package org.flightclub;

import java.awt.*;

/**
 * Created by turbo on 17.03.14.
 */
class Surface extends PolyLine {

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

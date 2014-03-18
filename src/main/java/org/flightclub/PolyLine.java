/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Graphics;

public class PolyLine {
    final int numPoints;
    final int[] points;
    int nextIndex = 0;
    final Object3d object3d;

    // true color
    Color c;

    // apparent color
    Color c_;

    boolean isSolid = false;
    boolean isVisible = false;

    Vector3d normal;

    public PolyLine(Object3d o, int inNumPoints, Color inColor) {
        numPoints = inNumPoints;
        points = new int[numPoints];
        object3d = o;
        c = inColor;
        c_ = inColor;
    }

    public void addPoint(int point) {
        points[nextIndex] = point;
        nextIndex++;
    }

    boolean isBackFace() {
        if (normal == null) return false;

        setNormal();    //now ???

        Vector3d p = object3d.points.elementAt(points[0]);
        Vector3d ray = new Vector3d();
        Tools3d.subtract(p, object3d.app.cameraMan.getEye(), ray);

        return (Tools3d.dot(normal, ray) >= 0);
    }

    void setNormal() {
        if (numPoints < 3) return;
        normal = new Vector3d();

        Vector3d[] ps = new Vector3d[3];
        for (int i = 0; i < 3; i++)
            ps[i] = object3d.points.elementAt(points[i]);

        Vector3d e1 = new Vector3d();
        Vector3d e2 = new Vector3d();
        Tools3d.subtract(ps[0], ps[1], e1);
        Tools3d.subtract(ps[2], ps[1], e2);

        Tools3d.cross(e1, e2, normal);
        normal.makeUnit();

        calcLight();
    }

    void calcLight() {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        float light = object3d.app.cameraMan.surfaceLight(normal);
        r *= light;
        g *= light;
        b *= light;

        c_ = new Color(r, g, b);
    }

    public void draw(Graphics g) {
        Vector3d a;
        Vector3d b;

        if (numPoints <= 1) return;
        g.setColor(this.getColor());

        for (int i = 0; i < numPoints - 1; i++) {
            a = object3d.points_.elementAt(points[i]);
            b = object3d.points_.elementAt(points[i + 1]);

            Boolean inFOV1 = object3d.inFOVs.elementAt(points[i]);
            Boolean inFOV2 = object3d.inFOVs.elementAt(points[i + 1]);

            //System.out.println(inFOV1.booleanValue() && inFOV2.booleanValue());

            if (inFOV1 && inFOV2) {
                g.drawLine((int) a.y, (int) a.z, (int) b.y, (int) b.z);
            }
        }
    }

    Color getColor() {
        //fogging
        Vector3d p = object3d.points_.elementAt(points[0]);
        return object3d.app.cameraMan.foggyColor(p.x, c_);
    }
}

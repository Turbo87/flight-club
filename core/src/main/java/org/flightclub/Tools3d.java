/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.ColorFactory;

import java.util.Vector;

/**
 * static methods for 3d geometry
 */
public class Tools3d {
    public static final float INFINITY = 999999;

    public static final int YZF = 0;
    public static final int YZB = 1;
    public static final int ZXF = 2;
    public static final int ZXB = 3;
    public static final int XYF = 4;
    public static final int XYB = 5;

    static int depth = 50;

    static Vector3d[] circleXZ(int numPoints, float radius, Vector3d center) {
        float dtheta = (float) Math.PI * 2 / numPoints;
        Vector3d[] circle = new Vector3d[numPoints];

        for (int i = 0; i < numPoints; i++) {
            float theta = dtheta * i;
            float x = (float) Math.sin(theta) * radius;
            float z = (float) Math.cos(theta) * radius;

            circle[i] = new Vector3d(x, 0, z);
            circle[i].add(center);
        }
        return circle;
    }

    /** get points for unit square in given plane */
    static Vector square(int face, float bottom, float left, float top, float right, float d) {
        Vector<Vector3d> sq = new Vector<>();
        Vector3d[] ps = new Vector3d[5];

        for (int i = 0; i < 5; i++) {
            ps[i] = new Vector3d(d, d, d);
            sq.addElement(ps[i]);
        }

        switch (face) {
            case YZF:
                ps[0].y = left;
                ps[0].z = bottom;
                ps[1].y = left;
                ps[1].z = top;
                ps[2].y = right;
                ps[2].z = top;
                ps[3].y = right;
                ps[3].z = bottom;
                break;

            case YZB:
                ps[0].y = left;
                ps[0].z = bottom;
                ps[3].y = left;
                ps[3].z = top;
                ps[2].y = right;
                ps[2].z = top;
                ps[1].y = right;
                ps[1].z = bottom;
                break;

            case ZXB:
                ps[0].x = left;
                ps[0].z = bottom;
                ps[1].x = left;
                ps[1].z = top;
                ps[2].x = right;
                ps[2].z = top;
                ps[3].x = right;
                ps[3].z = bottom;
                break;

            case ZXF:
                ps[0].x = left;
                ps[0].z = bottom;
                ps[3].x = left;
                ps[3].z = top;
                ps[2].x = right;
                ps[2].z = top;
                ps[1].x = right;
                ps[1].z = bottom;
                break;

            case XYF:
                ps[0].x = left;
                ps[0].y = bottom;
                ps[1].x = right;
                ps[1].y = bottom;
                ps[2].x = right;
                ps[2].y = top;
                ps[3].x = left;
                ps[3].y = top;
                break;

            case XYB:
                ps[0].x = left;
                ps[0].y = bottom;
                ps[3].x = left;
                ps[3].y = top;
                ps[2].x = right;
                ps[2].y = top;
                ps[1].x = right;
                ps[1].y = bottom;
                break;
        }

        ps[4].set(ps[0]);
        return sq;
    }

    static Vector unitSquare(int face, float d) {
        float dx = (float) 0.5;
        return square(face, -dx, -dx, dx, dx, d);
    }

    static Object3d unitCube(XCGame theApp, boolean isSolid) {
        Object3d cube = new Object3d(theApp);
        float d = (float) 0.5;
        Color c = ColorFactory.GREEN;

        cube.addWire(unitSquare(XYF, d), c, isSolid);
        c = ColorFactory.RED;
        cube.addWire(unitSquare(XYB, -d), c, isSolid);
        c = ColorFactory.BLUE;

        cube.addWire(unitSquare(YZF, d), c, isSolid);
        c = ColorFactory.MAGENTA;
        cube.addWire(unitSquare(YZB, -d), c, isSolid);
        c = ColorFactory.ORANGE;

        cube.addWire(unitSquare(ZXF, d), c, isSolid);
        c = ColorFactory.PINK;
        cube.addWire(unitSquare(ZXB, -d), c, isSolid);
        c = ColorFactory.YELLOW;

        return cube;
    }

    static float[][] identity() {
        return new float[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    }

    static float[][] zero() {
        return new float[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    }

    public static float[][] rotateX(Vector3d v) {
		/*
		 * rotation matix: rotate the given point so that it lies on the x axis
		 * 1. rotate about z axis
		 * 2. rotate about y axis
		 * 3. rotate about x axis (so up stays up)
		 */

        float r, r_;
        float[][] m1 = identity();
        float[][] m2 = identity();
        float[][] m3 = identity();
        float[][] m4 = identity();
        float[][] m5 = identity();

        r_ = (float) Math.sqrt(v.y * v.y + v.x * v.x);
        if (r_ != 0) {
            m1[0][0] = v.x / r_;
            m1[0][1] = v.y / r_;
            m1[1][0] = -m1[0][1];
            m1[1][1] = m1[0][0];
        }

        r = v.length();
        if (r_ != 0) {
            m2[0][0] = r_ / r;
            m2[0][2] = v.z / r;
            m2[2][0] = -m2[0][2];
            m2[2][2] = m2[0][0];
        }

        //keep z azis pointing up
        Vector3d up = new Vector3d(0, 0, 1);
        applyTo(m1, up, up);

        applyTo(m2, up, up);

        r_ = (float) Math.sqrt(v.y * v.y + v.z * v.z);
        if (r_ != 0) {
            m4[1][1] = up.z / r_;
            m4[1][2] = up.y / r_;
            m4[2][1] = -m4[1][2];
            m4[2][2] = m4[1][1];
        }

        m3 = applyTo(m2, m1);
        m5 = applyTo(m4, m3);
        //return m5;
        return m3;
    }

    public static void applyTo(float[][] m, Vector3d a, Vector3d a_) {
        float[] inV = new float[3];
        float[] outV = {0, 0, 0};

        inV[0] = a.x;
        inV[1] = a.y;
        inV[2] = a.z;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                outV[i] = outV[i] + m[i][j] * inV[j];
            }
        }
        a_.x = outV[0];
        a_.y = outV[1];
        a_.z = outV[2];
    }

    public static float[][] applyTo(float[][] m1, float[][] m2) {
        float[][] m3 = zero();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    m3[i][j] = m3[i][j] + m1[i][k] * m2[k][j];
                }
            }
        }
        return m3;
    }

    /**
     * return false if point falls outside field of
     *view (FOV), true otherwise
     */
    public static boolean projectYZ(Vector3d a, Vector3d a_, float d) {
        float tanMax = 25;

        a_.x = a.x;
        if (a.x >= d) return false; //point behind camera

        float tan;
        float scale = (d - a.x);

        tan = a.y / (d - a.x);
        if (tan * tan > tanMax) return false;
        else a_.y = a.y / scale;

        tan = a.z / (d - a.x);
        if (tan * tan > tanMax) return false;
        else a_.z = a.z / scale;

        return true;
    }

    public static double rnd(double lower, double upper) {
        return Math.random() * (upper - lower) + lower;
    }
}

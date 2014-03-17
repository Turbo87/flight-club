/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class Circuit {
    /*
        A list of points to fly round when ridge soaring.
        Usually two points, unless ridge is snakey.
        NB We use the hill's local coord system
    */
    final Hill hill;
    final Vector3d[] points;
    int numPoints = 0;
    int next = 0;
    final float lift = (float) 1.5;
    final float liftUpto = (float) 1.5;
    Vector3d fallLine;

    public Circuit(Hill inHill, int n) {
        hill = inHill;
        points = new Vector3d[n];

        //default fall line - climb one unit -> move north (+y) one unit
        fallLine = new Vector3d(0, 1, 0);
    }

    int turnDir() {
        if (next == 0) return MovementManager.RIGHT;
        else if (next == 1) return MovementManager.LEFT;
        else return 0;
    }

    void add(Vector3d inP) {
        /*
			hills build circuits using their local coords
		*/
        points[numPoints] = inP;
        numPoints++;
    }

    Vector3d next(FlyingDot flyingDot) {
        Vector3d p = points[next];
        next++;
        if (next > numPoints - 1) {
            next = 0;
        }

        //transform from local to global coords
        Vector3d q = new Vector3d();
        q.x = p.x + hill.x0;
        q.y = p.y + hill.y0;

        return q;
    }

    float getLift(Vector3d p) {
		/*
			refine this ?
		*/
        if (p.z <= liftUpto)
            return lift;
        else
            return 0;
        //when at top of left band want sink rate of zero or one ?
    }

    boolean atTop(Vector3d p) {
        return p.z >= liftUpto - (float) 0.01;
    }

}
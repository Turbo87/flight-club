package flightclub.client;

import java.awt.*;
import java.util.*;

/**
   A list of points to fly round when ridge soaring.  Usually two
   points, unless ridge is snakey.  NB We use the hill's local coord
   system.

   Also used for roads.
*/
class Circuit {
    float[][] points;
    int numPoints = 0;
    int next = 0;
    float[] fallLine;
    boolean closed = true; // flag - do we go back to start after reaching the last point
    boolean avoidHill = true;

    /**
       Creates a circuit with n points.
    */
    public Circuit (int n) {
	points = new float[n][3];
		
	//default fall line - climb one unit -> move north (+y) one unit
	fallLine = new float[] {0, 1, 0};
    }

    /**
       Creates a circuit with the given points. Used for roads.
    */
    public Circuit (float[][] ps) {
	points = ps;
	fallLine = new float[] {0, 0, 0};
	closed = false;
	avoidHill = false;
    }

    int turnDir() {
	if (next == 0) return MovementManager.RIGHT * 2;
	else if (next == 1) return MovementManager.LEFT * 2;
	else return 0;
    }
	
    void add(float[] p) {
	points[numPoints++] = p;
    }
	
    /**
       Returns the next point on the circuit. Or null if we have
       reached the end of the circuit.
    */
    float[] next() {
	float[] p;
	if (next <= points.length - 1) {
	    p = points[next];
	} else {
	    return null;
	}
	next++;
	if (closed && next > points.length - 1) {
	    next = 0;
	}
		
	// copy
	float[] q = new float[3];
	q[0] = p[0];
	q[1] = p[1];
		
	return q;
    }

    float[] pointAt(int i) {
	return points[i];
    }

    void reset() {
	next = 0;
    }
}

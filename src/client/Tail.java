/*
 * @(#)Tail.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.awt.*;
import java.util.*;
/**
   This class implements a tail of length n which may be attached to a
   particle. We maintain a list of points - the path of the particle
   over the last n ticks. We join the dots to draw the tail. Perhaps
   not all the dots...

   <pre>

   A tail that joins every other dot:

             +--+  +--+  +--+>>
	     tail            particle

   </pre>

   The alogorithm is not perfect. If the frame rate gets higher the tail
   will shorten because the distance travelled each tick will be
   less. Oh well !

   ? Design ? Should Tail extend Obj3d or have an Obj3d ?(cf a cloud
   has an Obj3d)
*/
class Tail extends Obj3d {
    Particle particle;
    int nPoints, nWires;
    Color color;
    private float[][] tail; // list of points
    int wireEvery;	// default - add a wire for every 4 points
    int[] vertMap;
	
    static final int NUM_WIRES = 10;//6;
    static final int WIRE_EVERY = 6;//4

    /** Creates a tail made from n wires. */
    public Tail(ModelViewer modelViewer, Particle particle, Color color, int nWires, int wireEvery) {
	super(modelViewer, 0, true);
	this.setNumPolywires(nWires);
	this.nWires = nWires;
	this.particle = particle;
	nPoints = (nWires - 1) * wireEvery + 2;
	this.color = color;
	this.wireEvery = wireEvery;
	vertMap = new int[nPoints * 2];
    }

    /** Creates a tail with default length and spacing. */
    public Tail(ModelViewer modelViewer, Particle particle, Color color) {
	this(modelViewer, particle, color, NUM_WIRES, WIRE_EVERY);
    }

    /**
       Builds the tail with all its points initially at p. We fudge so
       that each point is slightly different so that things will work
       when we add the wires.
     */
    public void init() {
	tail = new float[nPoints][3];
	float[] p = particle.p;

	for (int i = 0;i < nPoints;i++) {
	    tail[i][0] = p[0];
	    tail[i][1] = p[1] - (float) i/1000;
	    tail[i][2] = p[2];
	}
	
	for (int i = 0; i < nPoints - 1; i+= wireEvery) {
	    super.addPolywire(new float[][]{tail[i], tail[i+1]}, color);
	}

	/*
	  Define a mapping from 0..N to obj3d's point
	  indexes so we can update the points later.   
	*/
	for (int i = 0; i < nWires; i++ ) {
	    vertMap[i * 2] = this.getPointIndex2(i, 0);
	    vertMap[i * 2 + 1] = this.getPointIndex2(i, 1);
	}
    }
	
    /**
       Moves the tail to point p. The particle that owns the tail
       should call this method every tick.

       We move along the list from last to first copying the data from
       n - 1 to n. Finally, we copy the new p to the head of the
       list.
    */
    public void tick(float t, float dt) {
	float[] p = particle.p;

	// any wind drift ?
	float[] drift = new float[3];

	try {
	    Glider glider = (Glider) particle;
	    drift[0] = glider.air[0] * dt;
	    drift[1] = glider.air[1] * dt;
	} catch (Exception e) {;}

	for (int j = nPoints - 1; j > 0; j--) {
	    tail[j][0] = tail[j-1][0] + drift[0];
	    tail[j][1] = tail[j-1][1] + drift[1];
	    tail[j][2] = tail[j-1][2];
	}

	tail[0][0] = p[0];
	tail[0][1] = p[1];
	tail[0][2] = p[2];

	// update the 3d points used in the wires
	updateObj();
    }
	
    /**
	Moves entire tail to p (eg. after glider has landed and we
	move it to a new position to resume play).
    */
    public void reset() {
	float[] p = particle.p;
	for (int i = 0; i < nPoints; i++) {
	    tail[i][0] = p[0];
	    tail[i][1] = p[1];
	    tail[i][2] = p[2];
	}
	updateObj();
    }

    // update the 3d points used in the wires
    private void updateObj() {
	for (int i = 0; i < nWires; i++) {
	    int j = i * wireEvery;
	    this.setPoint(vertMap[i*2], tail[j][0], tail[j][1], tail[j][2]);
	    this.setPoint(vertMap[i*2+1], tail[j+1][0], tail[j+1][1], tail[j+1][2]);
	}
    }
}

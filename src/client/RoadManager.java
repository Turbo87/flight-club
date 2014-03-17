/*
 * @(#)RoadManager.java (part of 'Flight Club')
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
import java.io.*;
/**
   Creates the roads. Gets data by parsing the stream of
   tokens. Or gets passed default data array.
*/
public class RoadManager {
    XCModelViewer xcModelViewer;
    Road[] roads;

    public RoadManager(XCModelViewer xcModelViewer, StreamTokenizer st) throws IOException {
	this.xcModelViewer = xcModelViewer;
	//Tools3d.debugTokens(st);
	st.nextToken();
	if (! "NUM_ROADS:".equals(st.sval))
	    throw new FileFormatException("Unable to read number of roads: " + st.sval);
	st.nextToken();
	int n = (int) st.nval;
	roads = new Road[n];
	st.nextToken();	

	// create roads
	for (int i = 0; i < roads.length; i++) {
	    roads[i] = new Road(xcModelViewer, st);
	}
    }

    public RoadManager(XCModelViewer xcModelViewer, float[][][] pss) {
	this.xcModelViewer = xcModelViewer;
	roads = new Road[pss.length];
	for (int i = 0; i < pss.length; i++) {
	    roads[i] = new Road(xcModelViewer, pss[i]);
	}
    }

    void renderMe() {
	for (int i = 0; i < roads.length; i++) {
	    roads[i].renderMe();
	}
    }

    void asString() {
	for (int i = 0; i < roads.length; i++) {
	    roads[i].asString();
	}
    }
}

/**
   We record the 'snail' of points followed by a particle to define a
   road.
*/
class Road {
    XCModelViewer xcModelViewer;
    float[][] ps = new float[100][3]; //list of points that are joined by wires to create the road
    int numPoints = 0;
    float[][] circuit; // data points that define the road
    float turnRadius = 10;
    float speed = 1; //distance between wires

    /**
       Parse the file to read the circuit - a list of points (x,
       y). The file format is the number of points followed by comma
       seperated pairs of x and y co-ords...

       3 2.1 1.1, 3.2 1.0, 4.3 7,
    */
    Road(XCModelViewer xcModelViewer, StreamTokenizer st) throws IOException {
	this.xcModelViewer = xcModelViewer;

	st.nextToken();
	int n = (int) st.nval;
	circuit = new float[n][3];

	for (int i = 0; i < n; i++) {
	    st.nextToken();
	    circuit[i][0] = (float) st.nval;
	    st.nextToken();
	    circuit[i][1] = (float) st.nval;
	    st.nextToken(); // gobble comma
	}
	createSnail();
    }

    /**
       A constructor where the circuit is passed in. Used for the
       deafult task.
    */
    Road(XCModelViewer xcModelViewer, float[][] circuit) {
	this.xcModelViewer = xcModelViewer;
	this.circuit = circuit;
	createSnail();
    }
    
    /**
       We send a particle along the circuit and record the list of
       points it travels thru'. This list of points will be joined up
       to create a curvy road.
    */
    void createSnail() {
        PathBuilder pb = new PathBuilder(xcModelViewer, circuit, turnRadius, speed);
        while (!pb.moveManager.finishedCircuit) {
            pb.tick(0, 1);
        }
    }

    private Obj3d obj3d = null;
    static final Color COLOR_ROAD = new Color(255, 175, 175); // PINK

    void renderMe() {
	NodeManager nodeManager = xcModelViewer.xcModel.task.nodeManager;

	// start afresh
	if (obj3d != null) {
	    obj3d.destroyMe();
	}

	// filter snail to get points over loaded nodes, also organise
	// points into pairs
	float[][][] pss = new float[numPoints - 1][2][3];
	int n = 0;
	for (int i = 0; i < numPoints - 1; i++) {
	    if (nodeManager.contains(ps[i][0], ps[i][1])) {
		pss[n][0] = ps[i];
		pss[n][1] = ps[i + 1];
		n++;
	    }
	}

	obj3d = new Obj3d(xcModelViewer, 0, true);
	obj3d.setNumPolywires(n);

	for (int i = 0; i < n; i++) { // note we use n and *not* ps.length
	    obj3d.addPolywire(pss[i], COLOR_ROAD);
	}
    }

    void asString() {
	System.out.println("Road:: numPoints: " + numPoints);
    }

    /**
       This inner class implements a particle which follows a circuit. The
       path taken is stored in ps.
    */
    class PathBuilder extends Particle {
        MovementManager moveManager;

        public PathBuilder(XCModelViewer xcModelViewer, float[][] circuit, float turnRadius, float speed) {
            super(xcModelViewer);
	    this.speed = speed;
            this.turnRadius = turnRadius;

            xcModelViewer.clock.removeObserver(this); // do not tick

            moveManager = new MovementManager(xcModelViewer, this);
            moveManager.setCircuit(new Circuit(circuit));

            p = circuit[0];
	    initV(circuit);
        }

	/**
	   Sets v to point from the first to the second circuit point.
	*/
	private void initV(float[][] circuit) {
	    Tools3d.subtract(circuit[1], circuit[0], v);
	    Tools3d.makeUnit(v);
	}

        public void tick(float t, float dt) {
            nextTurn = moveManager.nextMove();
            super.tick(t, dt);

	    /**
	       Copy p to ps. Check array size and double if at end.
	    */
	    if (numPoints >= ps.length) {
		float[][]tmp = ps;
		ps = new float[tmp.length * 2][3];
		System.arraycopy(tmp, 0, ps, 0, tmp.length);
	    }
	    ps[numPoints++] = new float[] {p[0], p[1], p[2]};
        }

        public void createTail() { tail = null; }
    }
}

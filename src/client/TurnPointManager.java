/*
 * @(#)TurnPointManager.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.io.*;
/**
   Creates the turn points. Gets data by parsing the stream of
   tokens. Or creates default data if no stream is passed in.
*/
class TurnPointManager {
    XCModelViewer xcModelViewer;
    TurnPoint[] turnPoints;

    public TurnPointManager(XCModelViewer xcModelViewer, StreamTokenizer st) throws IOException {
	this.xcModelViewer = xcModelViewer;
	//Tools3d.debugTokens(st);
	st.nextToken();
	if (! "NUM_TURNS:".equals(st.sval))
	    throw new FileFormatException("Unable to read number of turn points: " + st.sval);
	st.nextToken();
	int n = (int) st.nval;
	turnPoints = new TurnPoint[n];
	st.nextToken();	

	// create turn points
	for (int i = 0; i < turnPoints.length; i++) {
	    turnPoints[i] = new TurnPoint(xcModelViewer, st);
	}
	linkTurnPoints();
    }

    /** Creates a manager with turn points at given locations. */
    public TurnPointManager(XCModelViewer xcModelViewer, float[] xs, float[] ys) {
	this.xcModelViewer = xcModelViewer;
	turnPoints = new TurnPoint[xs.length];
	for (int i = 0; i < xs.length; i++) {
	    turnPoints[i] = new TurnPoint(xcModelViewer, xs[i], ys[i]);
	}
	linkTurnPoints();
    }

    /**
       Makes a double linked list - sets next and prev for each item in
       the list of turn points.
    */
    private void linkTurnPoints() {
	for (int i = 0; i < turnPoints.length - 1; i++) {
	    turnPoints[i].setNextTP(turnPoints[i + 1]);
	}
	for (int i = 1; i < turnPoints.length; i++) {
	    turnPoints[i].setPrevTP(turnPoints[i - 1]);
	}

	// set distance from task start for each turn point
	float d = 0;
	for (int i = 0; i < turnPoints.length; i++) {
	    turnPoints[i].distanceFromStart = d;
	    d += turnPoints[i].distanceToNext;
	}
    }

    /**
       Returns the sum of the distances between the turn points.
    */
    float getTotalDistance() {
	float total = 0;
	for (int i = 0; i < turnPoints.length - 1; i++) {
	    total += turnPoints[i].distanceToNext;
	}
	return total;
    }

    /** Prints debug info. */
    void asString() {
	for (int i = 0; i < turnPoints.length; i++) {
	    turnPoints[i].asString();
	}
    }

    /** 
       Renders the turn points. What you get depends on which nodes
       are loaded.
    */
    void renderMe() {
	for (int i = 0; i < turnPoints.length; i++) {
	    turnPoints[i].renderMe();
	}
    }

    /**
       A bounding box for the whole task.
    */
    void boundingBox(float[] a, float[] b) {
	a[0] = b[0] = turnPoints[0].x;
	a[1] = b[1] = turnPoints[0].y;
	a[2] = b[2] = 0;

	for (int i = 1; i < turnPoints.length; i++) {
	    // x min, y min
	    if (turnPoints[i].x < a[0]) a[0] = turnPoints[i].x;
	    if (turnPoints[i].y < a[1]) a[1] = turnPoints[i].y;

	    // x max, y max
	    if (turnPoints[i].x > b[0]) b[0] = turnPoints[i].x;
	    if (turnPoints[i].y > b[1]) b[1] = turnPoints[i].y;
	}
    }
}


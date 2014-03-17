/*
 * @(#)TurnPoint.java (part of 'Flight Club')
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
import java.awt.Color;
/**
   This class implements a turn point. Each turn point defines a line
   which runs thru' it and the *next* turn point.

   Turn points do not have a mode (SLEEPING or AWAKE). Every time
   different nodes are loaded one should call renderMe(false) followed
   by renderMe(true).
 */
public class TurnPoint {
    XCModelViewer xcModelViewer;
    float x, y;
    float x_, y_; // offset into the sector
    TurnPoint nextTP = null;
    TurnPoint prevTP = null;
    float distanceToNext; // distance to fly to next turn point
    float distanceFromStart;

    /** Changes in x and y as you move a unit of distance along the
     * line from this turn point to the next. */
    float dx, dy; 

    // unique id for each instance of this class
    static int nextID = 0; 
    int myID;

    /** Define the sector a glider must enter in order to fly *around*
     * this turn point */
    static final double SECTOR_ANGLE = Math.PI/3;
    static final float SECTOR_DOT = (float) Math.cos(SECTOR_ANGLE);

    public TurnPoint(XCModelViewer xcModelViewer, StreamTokenizer st) throws IOException {
	this.xcModelViewer = xcModelViewer;
	st.nextToken();
	x = (float) st.nval;
	st.nextToken();
	y = (float) st.nval;
	st.nextToken(); // gobble end of line
	myID = nextID++; // unique id (for debugging)
    }

    public TurnPoint(XCModelViewer xcModelViewer, float x, float y) {
	this.xcModelViewer = xcModelViewer;
	this.x = x;
	this.y = y;
	myID = nextID++; // unique id (for debugging)
    }

    /**
       Sets the next turn point along the course. This allows us to
       define a line.
    */
    void setNextTP(TurnPoint nextTP) {
	this.nextTP = nextTP;
	calcLine();
    }

    /**
       Sets the previous turn point along the course. This allows us to
       define the sector for this turn point.
    */
    void setPrevTP(TurnPoint prevTP) {
	this.prevTP = prevTP;
	calcSector();
    }

    /**
       Defines the line joining this turn point to the next turn
       point.
    */
    private void calcLine() {
	if (nextTP == null) return;
	dx = nextTP.x - this.x;
	dy = nextTP.y - this.y;

	distanceToNext = (float) Math.sqrt(dx * dx + dy * dy);
	dx /= distanceToNext;
	dy /= distanceToNext;
    }

    /**
       Sets p to be a distance d along line from a to b.
    */
    void getPointOnLine(float d, float[] p) {
	p[0] = x + d * dx;
	p[1] = y + d * dy;
    }

    private float[] bisect = new float[3];
    private float[] bisectPerp = new float[3]; // for rendering

    /**
       We bisect the angle at this turn point to get vector
       <code>bisect</code> which defines the sector.
    */
    private void calcSector() {
	if (prevTP == null) return;
	bisect[0] = prevTP.dx - this.dx;
	bisect[1] = prevTP.dy - this.dy;
	Tools3d.makeUnit(bisect);
	Tools3d.cross(new float[] {0, 0, 1}, bisect, bisectPerp);

	// a point in the sector
	x_ = x + bisect[0];
	y_ = y + bisect[1];
    }

    /**
       Returns true if p is in sector. We take the dot product of the
       vector r pointing from this turn point to p and the vector
       <code>bisect</code> which defines the sector.

       Todo: debug
    */
    boolean inSector(float[] p) {
	float[] r = new float[3];
	Tools3d.subtract(p, new float[] {x, y, p[2]}, r);
	Tools3d.makeUnit(r);
	if (nextTP != null) {
	    return Tools3d.dot(r, bisect) >= SECTOR_DOT;
	} else {
	    // finish line - cos 90 degrees is zero
	    return Tools3d.dot(r, bisect) >= 0;
	}
    }

    /** Prints debug info. */
    void asString() {
	System.out.println("TurnPoint(" + myID 
			   + "): x=" + Tools3d.round(x) + ", y=" + Tools3d.round(y) 
			   + ", dx=" + Tools3d.round(dx) + ", dy=" + Tools3d.round(dy) 
			   + ", d1=" + distanceFromStart
			   + ", d2=" + distanceToNext);
    }

    /**
       Adds a visual representation of this turn point to the
       model. We draw...
       A. a pink sector on the ground and a line of arrows
       pointing to the next turn point.
       B. 
     */
    void renderMe() {
	if (prevTP == null) {
	    renderStart();
	    renderArrows();
	} else if (nextTP != null) {
	    renderSector();
	    renderArrows();
	} else {
	    renderFinish();
	}
    }

    /** Sector has radius and perpendicular width. */
    private static final float radius = Cloud.CLOUDBASE;
    private static final float dPerp = (float) Math.sin(SECTOR_ANGLE) * radius;
    private Obj3d objA = null; // start line , sector or finish line
    static final Color COLOR_SECTOR = new Color(255, 200, 0); //ORANGE;
    /** 
	Draws a pink triangle outline if turn point falls within a *loaded* node. 

	<pre>

	     dPerp
	.-------.
	 \  |  /
	  \ |r/
	   \|/
	    .(x, y)
	    
       </pre>
    */
    private void renderSector() {
	if (!xcModelViewer.xcModel.task.nodeManager.contains(x, y)){
	    if (objA != null) {
		objA.destroyMe();
		objA = null;
	    }
	    return;
	}

	// have we already created the sector ?
	if (objA != null) {
	    return;
	}

	float[][] ps = new float[3][3];
	float [] p = new float[3];
	Tools3d.linearSum(radius, bisect, -dPerp, bisectPerp, ps[1]);
	Tools3d.linearSum(radius, bisect, dPerp, bisectPerp, ps[2]);

	for (int i = 0; i < 3; i++) {
	    ps[i][0] += x;
	    ps[i][1] += y;
	}

	objA = new Obj3d(xcModelViewer, 0, true);
	objA.setNumPolywires(1);
	objA.addPolywireClosed(ps, COLOR_SECTOR);
    }

    private Obj3d objB = null; // arrows
    static final float ARROW_SPACING = Cloud.CLOUDBASE * 2;
    static final float ARROW_LEN = ARROW_SPACING * 0.3f;
    static final float ARROW_HEAD = ARROW_LEN * 0.2f;
    static final int ARROWS_MAX = 100; // just used to dim an array
    static final Color COLOR_ARROW = new Color(255, 200, 0);//ORANGE
    /**
       Draws a line of arrows. Note we only draw arrows that fall
       within a loaded node. 

       <pre>
              .c
               \
       a.-------.b
               / 
	      .d

       </pre>
    */
    private void renderArrows() {
	float[] a = new float[] {x, y, 0};
	float[] b = new float[]{a[0] + ARROW_LEN * dx, a[1] + ARROW_LEN * dy, 0};
	float[] c = new float[]{a[0] + (ARROW_LEN - ARROW_HEAD) * dx, 
				a[1] + (ARROW_LEN - ARROW_HEAD) * dy, 0};
	float[] d = new float[]{c[0], c[1], c[2]};
	c[0] += dy * ARROW_HEAD;
	c[1] += - dx * ARROW_HEAD;
	d[0] += - dy * ARROW_HEAD;
	d[1] +=  dx * ARROW_HEAD;

	float dx_ = dx * ARROW_SPACING;
	float dy_ = dy * ARROW_SPACING;
	float[][][] pss = new float[ARROWS_MAX][2][3]; // tails
	float[][][] qss = new float[ARROWS_MAX][3][3]; // heads
	NodeManager nodeManager = xcModelViewer.xcModel.task.nodeManager;

	// start afresh
	if (objB != null) {
	    objB.destroyMe();
	}

	int n = 0;
	float dist = 0;

	while (dist < distanceToNext) {
	    if (nodeManager.contains(a[0], a[1])) {
		pss[n][0][0] = a[0];
		pss[n][0][1] = a[1];
		pss[n][1][0] = b[0];
		pss[n][1][1] = b[1];

		qss[n][0][0] = c[0];
		qss[n][0][1] = c[1];
		qss[n][1][0] = b[0];
		qss[n][1][1] = b[1];
		qss[n][2][0] = d[0];
		qss[n][2][1] = d[1];

		n++;
		if (n > ARROWS_MAX) { // ideally, should not hit this
		    break;
		}
	    }
	    dist += ARROW_SPACING;
	    a[0] += dx_;
	    a[1] += dy_;
	    b[0] += dx_;
	    b[1] += dy_;
	    c[0] += dx_;
	    c[1] += dy_;
	    d[0] += dx_;
	    d[1] += dy_;
	}

	objB = new Obj3d(xcModelViewer, 0, true);
	objB.setNumPolywires(n * 2); // each arrow has two wires (head and tail)

	for (int i = 0; i < n; i++) { // note we use n and *not* ps.length
	    objB.addPolywire(pss[i], COLOR_ARROW);
	    objB.addPolywire(qss[i], COLOR_ARROW);
	}
    }

    /** Draws a perp line. */
    private void renderStart() {
	if (!xcModelViewer.xcModel.task.nodeManager.contains(x, y)){
	    if (objA != null) {
		objA.destroyMe();
		objA = null;
	    }
	    return;
	}

	float[][] ps = new float[2][3];
	ps[0][0] = x - dPerp * dy;
	ps[0][1] = y + dPerp * dx;
	ps[1][0] = x + dPerp * dy;
	ps[1][1] = y - dPerp * dx;

	objA = new Obj3d(xcModelViewer, 0, true);
	objA.setNumPolywires(1);
	objA.addPolywireClosed(ps, COLOR_SECTOR);
    }

    // for now...
    private void renderFinish() { 
	if (!xcModelViewer.xcModel.task.nodeManager.contains(x, y)){
	    if (objA != null) {
		objA.destroyMe();
		objA = null;
	    }
	    return;
	}

	float[][] ps = new float[2][3];
	ps[0][0] = x - dPerp * prevTP.dy;
	ps[0][1] = y + dPerp * prevTP.dx;
	ps[1][0] = x + dPerp * prevTP.dy;
	ps[1][1] = y - dPerp * prevTP.dx;

	objA = new Obj3d(xcModelViewer, 0, true);
	objA.setNumPolywires(1);
	objA.addPolywireClosed(ps, COLOR_SECTOR);
    }

}

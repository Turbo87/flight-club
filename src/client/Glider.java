/*
  Glider.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.client;

import flightclub.framework3d.*;
/**
   This class implements a glider. The class is abstract because any
   actual glider must have a controller. The possible controllers are
   AI, Network and User.  
*/
public class Glider extends MovingBody {
    XCModelViewer xcModelViewer;
    private String typeName;
    protected int typeID; // 0 - para, 1 - hang, 2 -sail
    private float[][] polar;
    protected int iP; // the current point on the polar 
    protected boolean landed = true;
    float[] air = new float[] {0, 0, 0}; // air movement
    private float ground = 0; // ground level
    protected float timeFlying = 0;

    // unique id for each instance of this class
    int myID;

    static int filmID = -1; // id of the glider that is being filmed

    /** Set this flag to true and glider will maintain a constant
     * height. */
    private boolean drone = false; 

    static final int SPEED = 0;
    static final int SINK = 1;

    /**
       Creates a glider using the spec given in the GliderType
       object. We create our own copy of the 3d object because it will
       have changing *state*. The other data is static so we may
       simply assign references.  
    */
    public Glider(XCModelViewer xcModelViewer, GliderType gliderType, int id) {
	super(xcModelViewer, new Obj3dDir(gliderType.obj, true));
	this.xcModelViewer = xcModelViewer;
	typeName = gliderType.typeName;
	typeID = gliderType.typeID;
	turnRadius = gliderType.turnRadius;
	polar = gliderType.polar;
	iP = 0;
	setPolar();
	this.myID = id; // my unique id
	/**
	   Wind does not change, so set once here for now, but...

	   TODO: 1. Make wind an observable that may change thru
	   the day.  2. Introduce wind shear - task designer may divide
	   air vertically into *two* layers.
	*/
	this.air[0] = xcModelViewer.xcModel.task.wind_x;
	this.air[1] = xcModelViewer.xcModel.task.wind_y;

	takeOff(false);
    }
    
    public void goFaster() { if (iP < polar.length - 1) iP++; setPolar(); }
    public void goSlower() { if (iP > 0) iP--; setPolar(); }
    public void setPolar(int iP) { this.iP = iP; setPolar(); }

    /** 
	Takes off starting at point p and heading in the direction
        given by the v[0] and v[1]. Note that v[2], the vertical
        component of v, is not used. The glide angle and speed are
        determined by the first point on the polar curve.  
    */
    private void takeOff(float[] p, float[] v) {
	landed = false;
	this.p[0] = p[0];
	this.p[1] = p[1];
	this.p[2] = p[2];
	this.v[0] = v[0];
	this.v[1] = v[1];
	iP = 0;
	setPolar();
	this.tail.reset();
	nextTurn = 0;
	timeFlying = 0;
    }

    private static final float TO_DIST = 0.5f; // seperate gliders at launch
    /**
       Start flying the task. We use the glider's unique id to ensure
       that gliders do not start on top of each other. The dummy flag
       enables a glider to positioned on the ground ready for take
       off.
     */
    public void takeOff() { takeOff(true); }

    public void takeOff(boolean really) {
	TurnPoint tp = xcModelViewer.xcModel.task.turnPointManager.turnPoints[0];
	float[] v = new float[] {tp.dx, tp.dy, 0};

	// choose dx and dy so gliders do not start on top of each
	// other. Rather, they spread out orthogonally to the course
	// line.
	float dx = (myID - 5) * TO_DIST * tp.dy;
	float dy = -(myID - 5) * TO_DIST * tp.dx;
	float[] p = new float[] {tp.x + dx, tp.y + dy, Cloud.CLOUDBASE * 0.75f}; 

	if (really) {
	    this.takeOff(p, v);
	} else {
	    this.p[0] = p[0];
	    this.p[1] = p[1];
	    this.p[2] = 0; // on the ground
	    this.v[0] = v[0];
	    this.v[1] = v[1];
	    this.tail.reset();
	    hitTheSpuds();
	}
    }

    /**
       Sets speed and v[2] to new values when we move to a new point
       on the polar curve. Things get a bit fiddly because v must
       remain a unit vector. So we scale the x and y components of v
       accordingly.

       <pre>

                 a
         .-------------.
           ----        |v[2]
	|v|=1  ----    |    
	           ----.


       </pre> */
    private void setPolar() { 
	if (polar[iP][SINK] >= polar[iP][SPEED] ||
	    polar[iP][SINK] <= - polar[iP][SPEED]) {
	    System.out.println("Invalid point on polar curve ! Sink must be less than speed.");
	    return;
	}
	speed = polar[iP][SPEED]; 
	v[2] = (!drone) ? polar[iP][SINK] : 0;
	v[2] /= speed; // v is a unit vector
	scaleVxy();
    }

    void hitTheSpuds() {
	speed = 0;
	nextTurn = 0;
	v[2] = 0;
	scaleVxy();
	landed = true;
    }

    /** 
	This utility fn is used by setPolar and hitTheSpuds. Without
        changing the direction of v in the xy plane change its length
        so that v has unit length. Note if v[2] equals 1 then there is
        no horizontal component to the motion. This will cause things
        to blow up pretty soon. The moral is keep sink < speed in all
        gliderType polar curves.  
    */
    private void scaleVxy() {
	float _length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]); 
	float length = (float) Math.sqrt(1 - v[2] * v[2]); 
	float fudge = length/_length;
	v[0] *= fudge; 
	v[1] *= fudge;
    }


    public void tick(float t, float dt) {
	if (!landed) {
	    // motion due to air (wind and lift/sink)
	    p[0] += air[0] * dt;
	    p[1] += air[1] * dt;
	    if (!drone) {
		p[2] += air[2] * dt;
	    }

	    // hit the spuds ?
	    if (p[2] <= ground) {
		hitTheSpuds();
	    }

	    timeFlying += dt;

	} else {
	    // nothing - will roll level ?
	}

	// motion due to velocity (and roll)
	super.tick(t, dt);

    }

    public final void setGround(float g) { ground = g; }
    public String getTypeName() { return typeName; }

    /** Returns the sink rate for a given point on the polar, or the
     * current point. */
    public float getSink(int i) { return polar[i][SINK]; }
    public float getSink() { return polar[iP][SINK]; }

    /** Returns the speed for a given point on the polar, or the
     * current point. */
    public float getSpeed(int i) { return polar[i][SPEED]; }
    public float getSpeed() { return polar[iP][SPEED]; }

    /**
       Returns a text message giving current state of glider. See
       GliderAI for user friendly stuff. This is for debug.
    */
    public String getStatusMsg() {
	if (!landed) {
	    return "P: " + Tools3d.asString(this.p);
	} else {
	    return "Landed !";
	}
    }

    public boolean getLanded() { return landed; }

    public float[] getFocus() {
	return new float[] {p[0], p[1], p[2]};
    }

    public float[] getEye() {
	return new float[] {p[0], p[1] - 3, p[2] + 0.3f};
    }

    /**
       Sets the drone flag so this glider maintains a contstant
       height. Handy for testing ? 
    */
    void makeDrone() { drone = true; }

    float glideAngle() {
	return polar[iP][SPEED]/-polar[iP][SINK];
    }

    // for debugging
    protected void finalize() {
	//System.out.println("Goodbye from glider(" + myID + ")");
    }

}


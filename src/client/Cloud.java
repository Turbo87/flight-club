/*
 * @(#)Cloud.java (part of 'Flight Club')
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
/**
   This class implements a cumulus cloud being fed by a thermal.

   The wind makes things slope:
   <pre>

                .--.
	 cloud /   |
	      .----.
	       / /
	thermal /    wind ->
	     / /
	--------------
	
   </pre>

   Inside the inner class Shape3d we work in spherical polars. Four
   quadrants are defined as follows:
   <pre>

	     2  |  1
	        |
	     ---.--->y
	        |
	     3  |  0
	        x

   </pre>
*/
public class Cloud implements CameraSubject, ClockObserver, LiftSource {
    XCModelViewer xcModelViewer;
    LifeCycle lifeCycle;
    private Shape3d shape3d;
    float x, y, h;

    // TODO: cloud params - seperate size from strength (see Trigger)
    private float size; // cloud size from 1 (small) to 5 (cu-nim !)

    private float wind_x, wind_y;
    private float slope_x, slope_y;
    private float myRnd; // see getEye
    private float liftMax;
    private Color color, color_;

    // unique id for each instance of this class
    static int nextID = 0; 
    int myID;

    float thermalRadius;
    float coreRadius;

    // lift unit * size gives max lift. choose so that cloud of size 1
    // gives lift a bit stronger than glider sink, size 2 gives a
    // decent climb
    static final float LIFT_UNIT = 0.08f;
    public static final int CLOUDBASE = 3; //TODO - make into an observable ? or a fn of site ?
    public static final int MAX_SIZE = 3;

    /**
	Creates a cloud. The cloud will grow from nothing, drift along
	for a period of time and then evaporate. The cloud strength is
	measured in multiples of a glider's sink rate. Stronger clouds
	are bigger and darker than weaker clouds.

	TODO: params - thermalStrenth, duration... ?size
    */
    public Cloud(XCModelViewer xcModelViewer, float x, float y, float size, float lifeSpan) {
	this.xcModelViewer = xcModelViewer;
	this.size = size; 
	this.setColor();

	this.x = x;
	this.y = y;
	this.h = CLOUDBASE;
	
	myID = nextID++;

	// assume life cycle starts from now
	lifeCycle = new LifeCycle(xcModelViewer.clock.getTime(), lifeSpan);
	shape3d = new Shape3d();

	xcModelViewer.clock.addObserver(this);

	// thermal strength at the core
	liftMax = size * LIFT_UNIT;

	thermalRadius = size/2;
	coreRadius = thermalRadius/3;

	// for camera angle variety
	myRnd = (float) (Tools3d.rnd(0,1));

	// wind
	setWind();

	// register with loaded nodes 
	registerWithNodes(true);

	//this.asString(); //tmp
    }

    /**
       Destroys refences to this cloud and its child objects so it
       will be garbage collected.  
    */
    public void destroyMe() {
	// asString();
	shape3d.obj3d.destroyMe();
	xcModelViewer.clock.removeObserver(this);
	registerWithNodes(false);
    }

    private Node[] myNodes = new Node[MAX_NODES];
    private static final int MAX_NODES = 5; //register with upto 3 overlapping nodes
    private int numNodes = 0; // registered with n nodes
    /**
       (un)registers cloud with the currently *loaded* nodes. When
       removing we skip the <code>contains</code> test for speed.

       NB. Wind drift means a cloud may move from one node to another
       over time.
     */
    private boolean registerWithNodes(boolean flag) {
	Node[] nodes;
	boolean registered = false;

	if (flag) {
	    nodes = xcModelViewer.xcModel.task.nodeManager.loadedNodes();
	    int next = 0;
	    for (int i = 0; i < nodes.length; i++) {
		if (nodes[i].contains(x, y)) {
		    nodes[i].add(this);
		    if (next >= MAX_NODES) { // TODO: tune max nodes
			System.out.println("Cloud over too many nodes !");
			this.asString();
			break;
		    }
		    myNodes[next++] = nodes[i];
		    registered = true;
		}
	    } 
	    numNodes = next;
	} else {
	    for (int i = 0; i < numNodes; i++) {
		myNodes[i].remove(this);
	    }
	    myNodes = new Node[MAX_NODES];
	    numNodes = 0;
	}
	return registered;
    }

    /**
       Returns true if the point is 'under' the cloud. By 'under' we
       mean within the sloping column of rising air that is feeding
       this cloud.  
    */
    public boolean contains(float[]p) {
	float cx, cy;
	
	cx = this.getX(p[2]);
	cy = this.getY(p[2]);

	// within bounding box ?
	if (p[0] > cx + thermalRadius) return false;
	if (p[0] < cx - thermalRadius) return false;
	if (p[1] > cy + thermalRadius) return false;
	if (p[1] < cy - thermalRadius) return false;
		
	return true;
    }
	
    public void tick(float t, float dt) {

	// get older
	lifeCycle.tick(t, dt);
	
 	// am i history ?
	if (lifeCycle.isDead()) {
	    destroyMe();
	    return;
	}

 	// am i not born yet (only possible if t reset by server) ?
	if (lifeCycle.notBorn()) {
	    destroyMe();
	    return;
	}

	// drift with the wind
	x += wind_x * dt;
	y += wind_y * dt;

	// update my 3d shape
	shape3d.updateMe(t, dt);

	/*
	  May have drifted into another node's zone. Also, if no
	  longer in an area covered by a loaded node then bye
	  bye. (NB. perhaps my origional node as now been unloaded.)
	*/
	registerWithNodes(false);
	if (!registerWithNodes(true)) {
	    destroyMe();
	}
    }
	
    /**
       Returns life span for a given size of cloud. Redundent now ?
    */
    public static float getLifeSpan(float size) {
	return LifeCycle.TIME_UNIT * size * 3; 
    }

    /** Returns max lift for a given size of cloud. */
    public static float getLift(float size) {
	return size * LIFT_UNIT; 
    }

    /** Sets the cloud to be at a certain age. */
    void setAge(float age) {
	float now = xcModelViewer.clock.getTime(); 
	lifeCycle.t0 = now - age;
	shape3d.dirty = true; // force recalc of shape
    }

    public float[] getFocus() {
	return new float[] {x, y + 2, CLOUDBASE/2};
    }
	
    public float[] getEye() {
	if (myRnd > 0.5) {
	    return new float[] {x + 1, y - 5, 1.5f};
	} else {
	    return new float[] {x, y - (2 + size * 2), CLOUDBASE/2};
	}
    }
	
    float getX(float z) {
	return x - (h - z) * slope_x;
    }

    float getY(float z) {
	return y - (h - z) * slope_y;
    }

    public float[] getP() {
	return new float[] {x, y, h};
    }

    /**
	Returns lift at a point. The lift is a function of r, the
	distance from the thermal center:

	<pre>

                  ^ lift
	      .---|---.
	    /     |     \
	  /       |       \
	.---------.---------.--->r
	          0

        </pre>
    */
    public float getLift(float[] p) {
	// TODO - optimize - use a helper class for *ALL* interpolated functions
	float dx = getX(p[2]) - p[0];
	float dy = getY(p[2]) - p[1];
	float r = (float) Math.sqrt(dx * dx + dy * dy);
	float lift, liftMax_;
       
	if (p[2] >= h) return 0; // no cloud flying

	if (!lifeCycle.isDecaying()) {
	    liftMax_ = liftMax;
	} else {
	    liftMax_ = lifeCycle.liftFudge() * liftMax;
	}

	if (r >= thermalRadius) {
	    return 0;
	} else if (r > coreRadius) {
	    return liftMax_ * (1 -  (r - coreRadius)/(thermalRadius - coreRadius));
	} else {
	    //we are in the core
	    return liftMax_;
	}
    }

    private void setColor() {
	final int COLOR = 245; //230
	final int COLOR_STEP = 10; //how much darker are strong clouds
	final int BASE_UP = 10; //20;

	// bigger clouds are darker
	int c = COLOR;
	if (size > 1) {
	    c -= (size - 1) * COLOR_STEP;
	}
	color = new Color(c, c, c);

	// make the base lighter because the 
	// cloud lets light thru' from above
	color_ = new Color(c + BASE_UP, c + BASE_UP, c + BASE_UP);
    }

    /**
       The thermal slope is a function of the wind. A thermal gets
       blown downwind as it climbs.  

       <pre>
                .--.
	       /   |
	      .----.
	        /
	       /      wind ->
	      /
	--------------

       </pre>

       In nil wind a thermal rises vertically and we have zero slope.
    */
    public void setWind() {
	Task task = xcModelViewer.xcModel.task;
	this.wind_x = task.wind_x;
	this.wind_y = task.wind_y;

	/* 
	   When the wind strength equals the lift we have a 1:1
	   slope. Well, that would be the case IF the thermal had no
	   inertia. In fact the thermal rises closer to vertical.  

	   This model gives a 1:1 slope when wind reaches WIND_MAX.
	*/
	// float WIND_MAX = xcModelViewer.xcModel.gliderManager.gliders[0].getSpeed(0); // no gliders yet !
	float WIND_MAX = 1.0f;
	slope_x = wind_x/WIND_MAX;
	slope_y = wind_y/WIND_MAX;

	// clamp values if very windy !
	if (slope_x > 1) slope_x = 1;
	if (slope_y > 1) slope_y = 1;
    }

    /*
      Keep the cloud in its mature stage so that its size does not change.
     */
    public void freeze() {
	lifeCycle.freeze();
    }

    /**
       This inner class implements a 3d shape for the cloud.

       The cloud is represented using a cube like shape made from 6
       polygons. The 'cube' is distorted - the points to lie on the
       surface of a hemi-sphere.

       We label the vertices as follows:

       <pre>

                 p6  .-----------.  p5
	            /           /|
	           /           / |
	      p7  .-----------.p4|
		  |  .(p2)    |  .  p1
		  |           | /
		  |           |/
	      p3  .-----------.  p0


       </pre>

       And map the verticies onto a hemi-sphere to get a cloud like
       shape:

       <pre>

	         p7  .---.  p4
		    /     \
		   /       \
	       p3 .---------.  p0
      
       </pre>

       Ok, so it doesn't look much like a cloud ! But it looks a bit like
       one cell of a cloud. 

    */
    class Shape3d {
	double[] theta = new double[4];
	double[] landa = new double[4];
	Obj3d obj3d;

	// pre compute some trig fns
	float[] thetaCos = new float[4];
	float[] thetaSin = new float[4];
	float[] landaCos = new float[4];
	float[] landaSin = new float[4];

	int[] vertMap = new int[8];

	float r0; // radius at base (vertices 0..3)
	float r1; // radius at top (vertices 3..7)
	boolean dirty = false;

	static final float SIZE_UNIT = 1.0f;

	public Shape3d() { 
	    setAngles(); 
	    obj3d = new Obj3d(xcModelViewer, 7, true);
	    setRadius();
	    addPolygons();
	}

	/** If the cloud is changing then change this shape. */
	public void updateMe(float t, float dt) {
	    if (lifeCycle.isGrowing() || lifeCycle.isDecaying() || dirty) {
		setRadius();
		updateShape();
	    } else {
		// no change in shape but we do have wind drift
		float dx = wind_x * dt;
		float dy = wind_y * dt;
		obj3d.translateBy(dx, dy, 0);
	    }
	    obj3d.updateShadow();
	    dirty = false;
	}

	/** 
	    Defines two angles for each quadrant:

	    <pre>

	       z
	       |
               |  .p4
               | /| 
               |/ | landa
               .*-|---------->y
	theta /*\ |
	     /   \|
	    /-----.p0
	   /
	  x
   


	    quadrants:

	    2  |  1                           
	    ---.----->y
	    3  |\ 0
	       |*\
	       x  \  *=theta

	    <pre>
	*/
	private void setAngles() {	
	    double lower, upper;
	    for (int quad = 0; quad < 4; quad++) {
		lower = quad * Math.PI/2;
		upper = lower + 0.9 * Math.PI/2;
		theta[quad] = Tools3d.rnd(lower, upper);
	    }

	    lower = 0.4 * Math.PI/2;
	    upper = 0.7 * Math.PI/2;
	    for (int quad = 0; quad < 4; quad++) {
		landa[quad] = Tools3d.rnd(lower, upper);
	    }

	    // pre compute cos and sin here for speed
	    for (int quad = 0; quad < 4; quad++) {
		thetaCos[quad] = (float) Math.cos(theta[quad]);
		thetaSin[quad] = (float) Math.sin(theta[quad]);
		landaCos[quad] = (float) Math.cos(landa[quad]);
		landaSin[quad] = (float) Math.sin(landa[quad]);
	    }
	}

	/**
	   Gets the new co-ords for each vertex and passes this data to obj3d.
	*/
	private void updateShape() {
	    for (int i = 0; i < 8; i++) {
		float[] p = getVert(i);
		obj3d.setPoint(vertMap[i], p[0], p[1], p[2]);
	    }
	    obj3d.setBB();
	    if (lifeCycle.isDecaying()) obj3d.setNormals();
	}		
	
	/**
	   Return co-ords of a vertex. See the *class* comments for an
	   explanation of the vertex labelling.  
	*/
	private float[] getVert(int index) {

	    float[] p;
	    int quad;
	    
	    if (index < 4) {
		quad = index;
		p = getVertBase(quad);
	    } else {
		quad = index - 4;
		p = getVertTop(quad);
	    }

	    // map from local to global co-ords 
	    p[0] += x;
	    p[1] += y;
	    p[2] += h;

	    return p;
	}

	private float[] getVertBase(int quad) {
	    float[] p = new float[3];
	    p[0] = r0 * thetaCos[quad];
	    p[1] = r0 * thetaSin[quad];
	    p[2] = 0;
	    return p;
	}

	private float[] getVertTop(int quad) {
	    float[] p = new float[3];
	    p[0] = r1 * landaCos[quad] * thetaCos[quad];
	    p[1] = r1 * landaCos[quad] * thetaSin[quad];
	    p[2] = r1 * landaSin[quad];

	    // wind slope - wind blows cloud tops downwind
	    p[0] += slope_x * p[2];
	    p[1] += slope_y * p[2];

	    return p;
	}

	private void addPolygons() {
	    float[][] ps = new float[8][];

	    // get the 8 vertices' co-ords
	    for (int i = 0; i < 8; i++) {
		ps[i] = getVert(i);
	    }

	    /* 
	       Add the 6 polygons. See the class comments above for an
               explanation of the vertex labelling. 
	    */

	    // front, back, left, right
	    obj3d.addPolygon(new float[][] {ps[0], ps[4], ps[7], ps[3]}, color);
	    obj3d.addPolygon(new float[][] {ps[2], ps[6], ps[5], ps[1]}, color);
	    obj3d.addPolygon(new float[][] {ps[7], ps[6], ps[2], ps[3]}, color);
	    obj3d.addPolygon(new float[][] {ps[5], ps[4], ps[0], ps[1]}, color);

	    // top, bottom 
	    /*
	      The top is bent (the four vertices do not lie in a
	      plane). The bottom is a lighter color (because the cloud
	      lets light thru' from above). 
	    */
	    obj3d.addPolygonBent(new float[][] {ps[4], ps[5], ps[6], ps[7]}, color, Obj3d.CONVEX);
	    obj3d.addPolygonWithShadow(new float[][] {ps[0], ps[3], ps[2], ps[1]}, color_, false);

	    /*
	      Define a mapping from my vertex labels to obj3d's point
	      indexes so we can update the points later.   
	    */
	    vertMap[0] = obj3d.getPointIndex(0, 0);
	    vertMap[4] = obj3d.getPointIndex(0, 1);
	    vertMap[7] = obj3d.getPointIndex(0, 2);
	    vertMap[3] = obj3d.getPointIndex(0, 3);

	    vertMap[2] = obj3d.getPointIndex(1, 0);
	    vertMap[6] = obj3d.getPointIndex(1, 1);
	    vertMap[5] = obj3d.getPointIndex(1, 2);
	    vertMap[1] = obj3d.getPointIndex(1, 3);
	}

	private void setRadius() {
	    /*
	      The clouds radius is a function of it's age.
	      here's the model...
			
	       - volume of rising air, dv, is constant.
	       - decay ~ surface area.
	       - dynamic equilibrium, dv = decay, at maturity.
	       - dv = 0 at old age.
	    */
	    float fn;
	    float age = lifeCycle.age;

	    // fudge for zero age
	    if (age == 0) {
		age = (float) lifeCycle.grow/100;
	    }

	    if (lifeCycle.isGrowing()) {
		fn = (float) Math.sqrt((double) age/lifeCycle.grow);
	    } else if (lifeCycle.isMature()) {
		fn = 1;
	    } else if (lifeCycle.isDecaying()) {
		fn = (float) Math.sqrt(1 - (double) (age - lifeCycle.mature)/(lifeCycle.decay - lifeCycle.mature));
	    } else {
		fn = 0;
	    }
	    r1 = fn * size * SIZE_UNIT;

	    /*
	      The base radius equals the top radius except when the cloud
	      is decaying. We make the base decay quicker than top of
	      cloud; clouds evaporate from the bottom (warmest) up.  
	    */
	    if (!lifeCycle.isDecaying()) {
		r0 = r1;
	    } else {
		fn = 2 * (fn - (float) 0.5);
		if (fn < 0.01) fn = (float) 0.01;
		r0 = r1 * fn;
	    }
	}
    }
	
    /**
       This class holds data for when things happen to the cloud. A
       cloud has three stages in this model: growth, maturity and
       decay.  

       <pre>

         r
	 |     .---------.
	 |   /             \
	 | /                 \
	 .-----.---------.-----.------->t
	0  grow   mature   decay

	 
       </pre>
    */
    class LifeCycle {
	float t0;
	float age = 0;
	float grow, mature, decay;
	boolean frozen = false;
	
	static final int TIME_UNIT = 15;

	/**
	   Sets time from birth until the end of each stage. The times
	   are a linear fn of the cloud size.  
	*/
	public LifeCycle (float t, float lifeSpan) {
	    // note when this cloud was created
	    t0 = t;

	    // how much time will the cloud spend in each stage of its life ?
	    grow = lifeSpan/3;
	    mature = grow + lifeSpan/3;
	    decay = lifeSpan;
	}

	boolean isGrowing() { return (age < grow); }
	boolean isDecaying() { return (age > mature); }
	boolean isMature() { return (age >= grow && age <= mature); }
	boolean isDead() { return (age > decay); }
	boolean notBorn() { return (age < 0); }

	// once half decayed we have no more lift
	boolean isActive() { return (age < mature + (decay - mature)/2); }

	void freeze() {
	    // stop the ageing process + goto the mature stage !
	    age = 0.99f * grow;
	    frozen = true;
	}

	void tick(float t, float dt) { 
	    if (!frozen) {
		age = t - t0; 
	    }
	}

	/**
	   Fn drops from 1 to -1 as cloud decays.
	*/
	float liftFudge() {
	    if (age > decay) return 0;
	    if (age < mature) return 1;
	    return 1 - (age - mature) * 2/(decay - mature);
	}
    }

    protected void finalize() {
	//System.out.println("Goodbye from cloud(" + myID + ")");
    }

    public boolean isActive() {
	return lifeCycle.isActive();
    }

    /** Returns true if the cloud will still be active t from now. */
    public boolean isActive(float t) {
	return lifeCycle.age + t <= lifeCycle.mature;
    }

    /** Used by node when searching. */
    public float getLift() {
	return liftMax;
    }

    void asString() {
	System.out.println("Cloud(" + myID + "): size = " + size 
			   + "x="  + Tools3d.round(x)
			   + ", y=" + Tools3d.round(y) 
			   + ", age=" 
			   + Tools3d.round(lifeCycle.age) + ", nNodes=" + numNodes);
    }
}


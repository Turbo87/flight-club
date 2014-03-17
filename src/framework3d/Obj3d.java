/*
  @(#)Obj3d.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.awt.*;
import java.util.*;
import java.io.*;
/**
   This class implements a 3d object made up of polygons. We have a
   list of points (x, y, z) in model space and a second list of the
   those points after they have been mapped into screen space (x_, y_,
   z_). The x_ coord is handy for fogging and sorting; it represents
   the 'depth' of the projected point. (y_, z_) is the location of the
   point in screen space.

   <p>The mapping from model space to screen space is as follows: 

   1. A translation such that the camera focus becomes the origin.
   2. A rotation such that the camera eye is on the +ve x_ axis.
   3. Forshorten y_ and z_ by one over x_, the depth of the point.
   4. Scale y_ and z_ according to the height of the display area in
   pixels.

   We use a flat array to store the coords as follows: (x0, y0, z0,
   x1, y1, z1, x2, ...).  If npoints * 3 exceeds the array size then
   we double the array size and do a System.copyarray(). 

   I got the flat array idea from the WireFrame demo applet.  
*/
public class Obj3d implements CameraSubject {
    protected ModelViewer modelViewer;
    float[] ps;
    float[] ps_;
    /** A flag for each point set to true if point can be seen by the camera. */
    private boolean[] visibles;
    private boolean visible; //if any points are visible then we say the object is visible
    int npoints = 0;
    int maxpoints;
    Polygon[] polygons;
    private int polynext = 0;
    Polywire[] polywires = new Polywire[0]; // hhm, tagged these on
    private int wirenext = 0;
    boolean noShade = false; // hack for sky

    /** Bounding box in model space */
    BB box = new BB();

    /** Depth bounding box (in screen space) */
    private float x_min, x_max;

    public static final int CONCAVE = 0;
    public static final int CONVEX = 1;
    
    // I *like* gray
    public static final Color COLOR_DEFAULT =  new Color(170, 170, 170);

    // shadowy bits
    static final int MAX_SHADOWS = 2;
    static final int SHADOW_COLOR = 180;
    int[] shadowCasters = new int[MAX_SHADOWS];
    Polygon[] shadows = new Polygon[MAX_SHADOWS];
    int numShadows = 0;

    /**
       Creates an Obj3d that may or may not be registed with the 3d
       object manager. Only registered objects are drawn on the
       screen. This constructor is private because it is only used by
       the parser. Everyone else should call the public constructor
       below which specifies the number of polygons that are used to
       model this object.  
    */
    private Obj3d(ModelViewer modelViewer, boolean register) { 
	this.modelViewer = modelViewer;
	if (register) {
	    registerObject3d();
	}

	// maxpoints is not a real limit because if we hit it we
	// double it
	maxpoints = 20;
	ps  = new float[maxpoints * 3];
	ps_ = new float[maxpoints * 3];
	visibles = new boolean[maxpoints]; 
	initShadow();
    }

    public Obj3d(ModelViewer modelViewer, int npolygons, boolean register) { 
	this(modelViewer, register);
	setNumPolygons(npolygons);
    }

    private void setNumPolygons(int n) {
	polygons = new Polygon[n];
    }

    // hack for glider tails !
    public void setNumPolywires(int n) {
	polywires = new Polywire[n];
    }

    /** Creates an Obj3d and registers it with the 3d object manager.  */
    public Obj3d(ModelViewer modelViewer, int npolygons) { this(modelViewer, npolygons, true); }

    /** Creates a copy of <code>from</code>. */
    public Obj3d(Obj3d from, boolean register) {
	this(from.modelViewer, from.polygons.length,  register);

	// make our storage same size as from's
	maxpoints = from.maxpoints;
	npoints = from.npoints;
	polynext = from.polynext;
	ps  = new float[maxpoints * 3];
	ps_ = new float[maxpoints * 3];
	visibles = new boolean[maxpoints]; 

	// copy co-ords data
	System.arraycopy(from.ps, 0, ps, 0, npoints * 3);

	// copy polygons
	for (int i = 0; i < from.polygons.length; i++) {
	    Polygon fromPoly = from.polygons[i];
	    polygons[i] = new Polygon(fromPoly.n, fromPoly.c, fromPoly.doubleSided);
	    System.arraycopy(fromPoly.points, 0, polygons[i].points, 0, fromPoly.points.length);
	}

	// copy shadows
	numShadows = from.numShadows;
	System.arraycopy(from.shadowCasters, 0, shadowCasters, 0, MAX_SHADOWS);
	for (int i = 0; i < numShadows; i++) {
	    Polygon fromPoly = from.shadows[i];
	    shadows[i] = new Polygon(fromPoly.n, fromPoly.c, fromPoly.doubleSided);
	    System.arraycopy(fromPoly.points, 0, shadows[i].points, 0, fromPoly.points.length);
	}
    }

    /** 
	Parses a text file and creates an Obj3d. The file format is as
	follows:

	<pre>

	# a simple kite made of two triangular polygons
	# by foo bar, 27 Aug 2002
	2
	t f CC99FF 12.1 2.4 3.5, 12.4 2.2 3.5, 3.2 5 7.4, 
	t t 336699 9.7 0 3.5, 2.2 2.2 3.5, 3.2 5 7.3,

	</pre>

	The above represents an Obj3d with two polygons, the second of
	which is double sided; each is a differnt color; each is made
	up of 3 points. Both cast shadows. The first flag indicates if
	the polygon casts a shadow. The second flag indicates if the
	polygon is double sided.
    */
    public Obj3d(StreamTokenizer st, ModelViewer modelViewer, boolean register) 
	throws IOException, FileFormatException {

	// call to constructor must be first, but we do not
	// know how many polygons yet, hence the following
	this(modelViewer, register);

	// gobble EOL's due to comments
	while (st.nextToken() == StreamTokenizer.TT_EOL) {;}

	// how many polygons
	int num = (int) st.nval;
	
	// set num polygons
	this.setNumPolygons(num);

	// gobble up new line
	st.nextToken();

	//Tools3d.debugTokens(st); //tmp
	//System.exit(0);

	// read a line a data for each polygon
	for (int i = 0; i < num; i++) {

	    // has shadow ?
	    st.nextToken();
	    boolean shadow = "t".equals(st.sval);
	    
	    // double sided ?
	    st.nextToken();
	    boolean doubleSided = "t".equals(st.sval);
	
	    // color - a hex number
	    Color color = null;
	    st.nextToken();
	    try {
		color = Color.decode("0x" + st.sval);
	    } catch (NumberFormatException e) {
		throw new FileFormatException("Unable to parse color: " + st.sval + ", polygon index: " + i);
	    }

	    if (color == null) {
		color = COLOR_DEFAULT;
	    }

	    // read point co-ords until we reach end of line
	    Vector points = new Vector();	    
	    while(st.nextToken() != StreamTokenizer.TT_EOL) {

		float[] p = new float[3];		

		p[0] = (float) st.nval;
		st.nextToken();
		p[1] = (float) st.nval;
		st.nextToken();
		p[2] = (float) st.nval;

		// gobble up comma which seperates points
		// note there must be a comma after the last point
		if (st.nextToken() != ',') {
		    throw new FileFormatException("Unable to parse co-ordinates; comma expected: " + st);
		}
		points.addElement(p);
	    }
	    
	    // convert vector to an array
	    float[][] vs = new float[points.size()][];
	    for (int j = 0; j < points.size(); j++) {
		vs[j] = (float[] ) points.elementAt(j);
	    }

	    // finally, add the polygon
	    if (!shadow) {
		this.addPolygon(vs, color, doubleSided);
	    } else {
		this.addPolygonWithShadow(vs, color, doubleSided);
	    }
	}
    }

    /**
       This method is a hack. I really want another constructor. 
    */
    public static Obj3d parseFile(InputStream is, ModelViewer modelViewer, boolean register) 
	throws IOException, FileFormatException {

	StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
	st.eolIsSignificant(true);
	st.commentChar('#');

	Obj3d obj3d = new Obj3d(st, modelViewer, register);

	// should be at end of file
	is.close();
	if (st.ttype != StreamTokenizer.TT_EOF)
	    throw new FileFormatException(is.toString());

	return obj3d;
    }

    public void destroyMe(){modelViewer.obj3dManager.removeObj(this);}
    private void registerObject3d() {modelViewer.obj3dManager.addObj(this);}
  
    /**
       Draws a 2d representation of the object onto the screen.

       @see ModelCanvas#paintModel
    */
    public void draw(Graphics g) {
	if (!visible) {
	    return;
	}
	final float[] eye = modelViewer.cameraMan.getEye();

	for (int i = 0;i < polygons.length;i++) {
	    //if (polygons[i] == null) {
	    //System.out.println("Warning: polygon is null");
	    //break;
	    //}
	    if (!polygons[i].isBackFace(eye)) polygons[i].draw(g);
	}

	for (int i = 0;i < polywires.length;i++) {
	    polywires[i].draw(g);
	}
	drawShadow(g);
    }
	
    /**
       Uses the camera to map all points from model space, (x, y, z)
       to screen space (x_, y_, z_). Remember that x_ represents the
       depth of the point and (y_, z_) maps to the screen (x, y)
       co-ords of the point.

       @see CameraMan 
    */
    public void transform() {

	final float[] f = modelViewer.cameraMan.getFocus();
	final float[][] m = modelViewer.cameraMan.getMatrix();
	final float d = modelViewer.cameraMan.getDistance();
	float[] a = new float[3];
	
	visible = false; //set true if any points are visble to the camera 
	for (int i=0; i < npoints * 3; i += 3) {
	    
	    a[0] = ps[i] - f[0];
	    a[1] = ps[i + 1] - f[1];
	    a[2] = ps[i + 2] - f[2];
	    Tools3d.applyTo(m, a, a);
	    visibles[i/3] = Tools3d.projectYZ(a, a, d);
	    ps_[i] = a[0];

	    if (visibles[i/3]) {
		modelViewer.cameraMan.scaleToScreen(a);
		ps_[i + 1] = a[1];
		ps_[i + 2] = a[2];
		visible = true;
	    }

	    //depth bounding box
	    if (i == 0) {
		x_min = x_max = ps_[i];
	    } else {
		if (ps_[i] < x_min) x_min = ps_[i];
		if (ps_[i] > x_max) x_max = ps_[i];
	    }
	}
    }
	
    public void translateBy(float dx, float dy, float dz) {
	for (int i = 0; i < npoints * 3; i += 3) {
	    ps[i] = ps[i] + dx;
	    ps[i + 1] = ps[i + 1] + dy;
	    ps[i + 2] = ps[i + 2] + dz;
	}
	//reset bounding box
	this.box.translateBy(dx, dy, dz);
    }

    public void scaleBy(float s) {
	for (int i = 0; i < npoints * 3; i += 3) {
	    ps[i] *= s;
	    ps[i + 1] *= s;
	    ps[i + 2] *= s;
	}
	//reset bounding box
	this.box.scaleBy(s);
    }

    /** Gives all polygons the specifed color. */
    public void setColor(Color c) {
	for (int i = 0; i < polygons.length; i++) {
	    polygons[i].c = c;
	}
    }

    /**
       Adds a point to the list. First scan thru' the list to see if
       we already have a point with identical coords. If so, return
       index of that point.  */
    int addPoint(float x, float y, float z) {
	int index = -1;
	for (int i = 0; i < npoints * 3; i += 3) {
	    if (ps[i] == x && ps[i + 1] == y && ps[i + 2] == z) { 
		index = i;
		break;
	    }
	}
		
	if (index != -1 ) {
	    //we already have this point - just return its index
	    return index;
	} else {
	    //add point
	    if (npoints >= maxpoints) {
		//double array size
		maxpoints *= 2;
		float[] qs = new float[maxpoints * 3];
		System.arraycopy(ps, 0, qs, 0, ps.length);
		ps = qs;
		
		ps_ = new float[maxpoints * 3];
		visibles = new boolean[maxpoints];
	    }

	    int i = npoints * 3;
	    ps[i] = x;
	    ps[i + 1] = y;
	    ps[i + 2] = z;

	    npoints++;
	    return i;
	}
    }

    /**
       Adds a polygon. Note that the vertices of the polygon are passed
       using a float[][] and NOT a float[]. Eg. 
       
         {{x0, y0, z0}, {x1, y1, z1}, ...}  

       We *flatten* the data once it is encapsulated inside this
       class; outside this class we want clarity; inside this class we
       want speed !  
    */
    public int addPolygon(float[][] vs, Color c, boolean doubleSided) {
	Polygon polygon = new Polygon(vs.length, c, doubleSided);
	for (int i = 0; i < vs.length; i++) {
	    polygon.addPoint(this.addPoint(vs[i][0], vs[i][1], vs[i][2]));
	}
	polygons[polynext] = polygon;

	//if this was the last polygon then we may now set the
	//bounding box
	if (polynext == polygons.length - 1) {
	    this.box.setBB();
	}
	return polynext++;
    }

    /** 
	Adds a polygon with one side visible. Which of the two sides
        is visible is determined by the order of the points. If as you
        look at the polygon the points go clockwise then the normal
        points away from you.  
    */
    public int addPolygon(float[][] vs, Color c) { return addPolygon(vs, c, false); }

    /** Adds a single sided gray polygon. */
    public int addPolygon(float[][] vs) { return addPolygon(vs, COLOR_DEFAULT, false); }

    /** Adds a polygon that is visible from both sides. */
    public int addPolygon2(float[][] vs, Color c) { return addPolygon(vs, c, true); }

    /** Adds a gray polygon that is visible from both sides. */
    public int addPolygon2(float[][] vs) { return addPolygon(vs, COLOR_DEFAULT, true); }

    /**
       Adds a bent polygon (made out of two triangles). We are passed
       four points that do NOT lie in a plane.

       <pre>

       p3 .-------.  p2
	  |     / |
	  |   /   |
	  | /     |
       p0 .-------.  p1

       </pre>

    */
    public void addPolygonBent(float[][] ps, Color color, int concaveFlag) {
	float h1 = ps[0][2] + ps[2][2];
	float h2 = ps[1][2] + ps[3][2];
		
	if (ps.length != 4) {
	    return;
	}

	if (h1 <= h2 && concaveFlag == CONCAVE || h1 >= h2 && concaveFlag == CONVEX) {
	    this.addPolygon(new float[][] {ps[0], ps[1], ps[2]}, color);
	    this.addPolygon(new float[][] {ps[2], ps[3], ps[0]}, color);
	} else {
	    this.addPolygon(new float[][] {ps[0], ps[1], ps[3]}, color);
	    this.addPolygon(new float[][] {ps[2], ps[3], ps[1]}, color);
	}
    }

    /**
    	Adds a series of polygons by rotating the points list (ps) steps times, and colouring
	them as defined by the color array (stripes!).
    */
    public void lathePolygons(float[][] ps, Color[] color, int steps) {
    	float _ps[][] = new float[ps.length][3];
	float stepAngle = (float)Math.PI * 2 / steps; // the angle of each step in radians
	int currColor = 0;
	float m[][];

    	for (int step = 0; step < steps; step++){

	    // translate each point to it next step round
	    m = Tools3d.rotateAboutZ(stepAngle);
	    for (int i = 0; i < ps.length; i++){
	        Tools3d.applyTo(m, ps[i], _ps[i]);
	    }

	    // build the polygons
	    for (int point = 0; point < ps.length - 1; point++){
		this.addPolygon(new float[][] {_ps[point], ps[point], ps[point + 1], _ps[point + 1]}, color[currColor]);
	    }

	    // copy the translated array to the starting array read for the next iteration
	    for (int i = 0; i < ps.length; i++){
	    	ps[i][0] = _ps[i][0];
		ps[i][1] = _ps[i][1];
		ps[i][2] = _ps[i][2];
	    }

	    currColor++;
	    if (currColor == color.length)
	    	currColor = 0;
	}
    }

    /** Adds a wire (eg. glider tails) */
    public int addPolywire(float[][] vs, Color c) {
	Polywire wire = new Polywire(vs.length, c);
	for (int i = 0; i < vs.length; i++) {
	    wire.addPoint(this.addPoint(vs[i][0], vs[i][1], vs[i][2]));
	}
	polywires[wirenext] = wire;
	return wirenext++;
    }

    /** Adds a closed wire (eg. an outline of a circle) */
    public int addPolywireClosed(float[][] vs, Color c) {
	Polywire wire = new Polywire(vs.length + 1, c);
	for (int i = 0; i < vs.length; i++) {
	    wire.addPoint(this.addPoint(vs[i][0], vs[i][1], vs[i][2]));
	}
	// close the wire by adding the first point onto its end
	wire.addPoint(this.addPoint(vs[0][0], vs[0][1], vs[0][2]));
	polywires[wirenext] = wire;
	return wirenext++;
    }

    /**
       Reverses the ordering of the polygons that make up this
       object. This may be useful for fudging the z order before
       drawing the object.
    */
    void reverse() {
	for (int i = 0; i< polygons.length/2 - 1; i++) {
	    int j = polygons.length - 1 - i;
	    Polygon p = polygons[i];
	    polygons[i] = polygons[j];
	    polygons[j] = p;
	}
    }

    /**
       Creates a unit cube whose centre of mass lies at the origin.
    */
    public static Obj3d makeCube(ModelViewer modelViewer) {

	Obj3d o = new Obj3d(modelViewer, 6);

	/**
	   Two steps. First we create a cube whose corner is at the
           origin and then we translate it so that its center is at
           the origin.
	*/

	//front (x=1) then back (x=0)
	o.addPolygon(new float[][] {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}}, Color.red);
	o.addPolygon(new float[][] {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}}, Color.pink);

	//right (y=1) then left (y=0)
	o.addPolygon(new float[][] {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}}, Color.green);
	o.addPolygon(new float[][] {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}}, Color.yellow);

	//top (z=1) then bottom (z=0)
	o.addPolygon(new float[][] {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}}, Color.blue);
	o.addPolygon(new float[][] {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}}, Color.magenta);

	o.translateBy(-0.5f, -0.5f, -0.5f);
	return o;
    }

    public void setBB() {
	this.box.setBB();
    }

    public float[] getEye() {
	return this.box.getEye();
    }

    public float[] getFocus() {
	return this.box.getCenter();
    }

    /** Gets the index of a point on a polygon. */ 
    public int getPointIndex(int poly, int vertex) {
	return polygons[poly].points[vertex];
    }

    /** Gets the index of a point on a polywire. (See Tail.java) */ 
    public int getPointIndex2(int poly, int vertex) {
	return polywires[poly].points[vertex];
    }

    /** Sets the co-ords of a point. */
    public void setPoint(int index, float x, float y, float z) {
	ps[index] = x;
	ps[index + 1] = y;
	ps[index + 2] = z;
    }

    public final float getDepthMin() { return x_min; }
    public final float getDepthMax() { return x_max; }
    
    /** Dumps the data of this 3d shape. */
    public String toString() {

	StringBuffer sb = new StringBuffer();

	// number of polygons
	sb.append(polygons.length + "\n");

	// loop - one line per polygon
	for (int i = 0; i < polygons.length; i++) {
	    Polygon po = polygons[i];
	    
	    //todo: tidy up shadows - what a mess !
	    boolean shadow = false;
	    for (int j =0; j < MAX_SHADOWS; j++) {
		if (shadowCasters[j] == i) {
		    shadow = true;
		    break;
		}
	    }
	    sb.append(shadow ? "t " : "f ");

	    sb.append(po.doubleSided ? "t " : "f ");

	    int rgb = po.c.getRGB();
	    sb.append("\"" + Integer.toHexString(rgb & 0xFFFFFF) + "\" ");
	    
	    // loop - points in this polygon
	    for (int j = 0; j < po.n; j ++) {
		int index = po.points[j];
		sb.append(Tools3d.round(ps[index]) 
			  + " " + Tools3d.round(ps[index + 1]) 
			  + " " + Tools3d.round(ps[index + 2]) 
			  + ", ");
	    }
	    
	    // finished this polygon so end line
	    sb.append("\n");
	}
	return new String(sb);
    }

    /** 
	Loops thru' surfaces setting their dirtyNormal flags so that
	the surface normals will be computed again.

	@see Obj3dDir.updateRotated
    */
    public void setNormals() {
	for (int i = 0; i < polygons.length; i++) {
	    polygons[i].dirtyNormal = true;
	}
    }

    /**
       Adds a polygon. And then add a second polygon - the shadow -
       using the same points with z ~ 0
    */
    public int addPolygonWithShadow(float[][] vs, Color c, boolean doubleSided) {
	shadowCasters[numShadows] = this.addPolygon(vs, c, doubleSided);
		
	//create shadow
	float[][] vs_ = new float[vs.length][3];
		
	//reverse point order so shadow faces 
	//up ?? assumes shadow caster faces down always ??
	int j = 0;
	for (int i = vs.length - 1; i >= 0; i--) {
	    float[] p = vs[i];
	    float[] q = new float[] {p[0], p[1], 0.00013f}; //change z so we get unique points
	    vs_[j++] = q;
	}
		
	Color color = new Color(SHADOW_COLOR, SHADOW_COLOR, SHADOW_COLOR);
	//Color color = new Color(80, 170, 80);
	shadows[numShadows] = new Polygon(vs_.length, color, false);
		
	for (int i = 0; i < vs_.length; i++) {
	    int pointIndex = this.addPoint(vs_[i][0], vs_[i][1], vs_[i][2]);
	    shadows[numShadows].addPoint(pointIndex);
	}
		
	numShadows++;
	return shadowCasters[numShadows-1];
    }
	
    private void initShadow() {
	for (int i = 0; i < MAX_SHADOWS; i++) {
	    shadowCasters[i] = -1;
	}
    }


    /**
       Keeps the shadow under its object. The owner/creator of this object
       should call this method each time they move the object.
    */
    public void updateShadow() {
	for (int i = 0; i < MAX_SHADOWS; i++) {
	    if (shadowCasters[i] == -1) return;
	    Polygon polygon = polygons[shadowCasters[i]];
			
	    for (int j = polygon.n - 1; j >= 0 ; j--) {
		float x = ps[polygon.points[j]];
		float y = ps[polygon.points[j] + 1];
		int index = shadows[i].points[polygon.n - 1 - j];
		ps[index] = x;
		ps[index + 1] = y;
		ps[index + 2] = 0;

		// old
		/*
		float[] p = (Vector3d) points.elementAt(surface.points[j]);
		float[] q = (Vector3d) points.elementAt(shadows[i].points[surface.numPoints - 1 - j]);//??
		Tools3d.clone(p, q);
		if (app.landscape != null) q.z = app.landscape.getHeight(q.x, q.y); else q.z = 0;
		*/
	    }
	}
    }
	
    public void drawShadow(Graphics g) {
	final float[] eye = modelViewer.cameraMan.getEye();
	for (int i = 0; i < MAX_SHADOWS; i++) {
	    if (shadowCasters[i] == -1) return;
	    if (!shadows[i].isBackFace(eye)) shadows[i].draw(g);
	}
    }

    /**
       This inner class represents a polygon. The polygon is made from
       N points (or vertices). It is either only visible from one side
       or it is visible from both sides.
    */
    class Polygon {
	int n; //number of points eg. 4 for a square
	int[] points; //list of indexes for the points that make up this Polygon
	int next = 0;
	Color c;	//true color
	boolean doubleSided = false;
	float[] normal;
	boolean dirtyNormal = true; // when true the normal needs to be reset by calling setNormal
	
	//for drawing
	int[] xs;
	int[] ys;				   

	public Polygon(int n, Color color, boolean doubleSided) {
	    this.n = n;
	    points = new int[n];
	    c = color;
	    this.doubleSided = doubleSided;
	    xs = new int[n];
	    ys = new int[n];
	}

	public void addPoint(int index) {
	    points[next] = index;
	    next++;
	}

	// some state for speeding up 'isBackFace'
	private float[] p = new float[3]; 
	private float[] ray = new float[3];

	/**
	   Decides if this polygon is a 'back face'. A back face is a
	   polygon that is facing away from the camera and therefore
	   should not be drawn. If this polygon is double sided then
	   it can not be a back face. In such a case this routine has
	   the side effect of ensuring that the normal points towards
	   the camera eye rather than away from it.

	   @see Obj3d#draw(Graphics) 
	*/
	boolean isBackFace(final float[] eye) {

	    if (dirtyNormal) {
		setNormal();
	    }

	    p[0] = ps[points[0]];
	    p[1] = ps[points[0] + 1];
	    p[2] = ps[points[0] + 2];

	    Tools3d.subtract(p,eye,ray);

	    if (doubleSided) {
		//flip the normal if necessary so this is NOT a backface
		if (Tools3d.dot(normal,ray) >= 0) {
		    Tools3d.scaleBy(normal, -1);
		}
		return false;
	    } else {
		return (Tools3d.dot(normal,ray) >= 0);
	    }
	} 

	/**
	   Creates the unit normal to the polygon by taking the cross
	   product of the first two edges. Call with 'nsides' set to two
	   if the polygon is visible from both sides.  
	*/
	void setNormal() {
	    /** We need three points to define two edges. */
	    if (n < 3) return;
	    if (normal == null) normal = new float[3];

	    float[] p0 = {ps[points[0]], ps[points[0] + 1], ps[points[0] + 2]};
	    float[] p1 = {ps[points[1]], ps[points[1] + 1], ps[points[1] + 2]};
	    float[] p2 = {ps[points[2]], ps[points[2] + 1], ps[points[2] + 2]};
	    
	    float[] e1 = new float[3];
	    float[] e2 = new float[3];

	    Tools3d.subtract(p1,p0,e1);
	    Tools3d.subtract(p2,p1,e2);

	    Tools3d.cross(e1,e2,normal);
	    Tools3d.makeUnit(normal);

	    dirtyNormal = false;
	}
	
	/**
	   Calculates the apparent color of this polygon. We ask the
	   camera how much light falls on a surface wiith this normal
	   and then darken the color accordingly.
	*/
	private Color calcLight() {
	    if (noShade) {
		return c;
	    }

	    int r = c.getRed();
	    int g = c.getGreen();
	    int b = c.getBlue();
		
	    float light = modelViewer.cameraMan.surfaceLight(normal);

	    r *= light;
	    g *= light;
	    b *= light;
		
	    return new Color(r,g,b);
	}
	
	/** Calculates the apparent color in two stages: lighting then fogging. */
	Color getColor(){
		return modelViewer.cameraMan.foggyColor(ps_[points[0]],calcLight());
	}

	/**
	   Draws this polygon on the screen. If any of the points are
	   not visible (ie. behind the camera) then do not attempt to draw
	   this polygon.  
	*/
	void draw(Graphics g) {
	    if (n <= 1) return;
	    g.setColor(getColor());

	    for (int i = 0; i < n ; i++) {
		if (!visibles[points[i]/3]) return;
		xs[i] = (int) ps_[points[i] + 1];
		ys[i] = (int) ps_[points[i] + 2];
	    }

	    g.fillPolygon(xs,ys,xs.length);
	}

    }


    /**
       This inner class represents a wire. The wire is made from
       N points (or vertices).
    */
    class Polywire {
	int n; //number of points eg. 4 for a square
	int[] points; //list of indexes for the points that make up this Polygon
	int next = 0;
	Color c;	//true color

	//for drawing
	int[] xs;
	int[] ys;				   

	public Polywire(int n, Color color) {
	    this.n = n;
	    points = new int[n];
	    c = color;
	    xs = new int[n];
	    ys = new int[n];
	}

	public void addPoint(int index) {
	    points[next++] = index;
	}

	/** Calculates the apparent color due to fogging. */
	private Color getColor(){
	    return modelViewer.cameraMan.foggyColor(ps_[points[0]],c);
	}

	/**
	   Draws this polywire on the screen. If a point is not
	   visible (ie. behind the camera) then do not attempt to draw
	   the line connecting that point.

	   Loop thru the list and draw a line connecting each point to
	   the next.
	*/
	void draw(Graphics g) {
	    int x1, y1, x2, y2;
	    if (n <= 1) return;
	    g.setColor(this.getColor());
		
	    for (int i = 0; i < n - 1; i++) {
		x1 = (int) ps_[points[i] + 1];
		y1 = (int) ps_[points[i] + 2];
		x2 = (int) ps_[points[i + 1] + 1];
		y2 = (int) ps_[points[i + 1] + 2];
		if (visibles[points[i]/3] && visibles[points[i + 1]/3]) {
		    g.drawLine(x1, y1, x2, y2);
		}
	    }
	}

    }

    public void setNoShade() {
	noShade = true;
    }

    public float getZmax() { return box.zmax; }

    /**
       This class implements a bounding box in model space that
       contains the parent Obj3d instance.  
    */
    class BB {
	float xmin, xmax, ymin, ymax, zmin, zmax;
	public BB() {;}

	void setBB() {
	    /**
	       Loop thru' all points and find the min and max
	       for each dimension.
	    */
	    for (int i = 0; i < npoints * 3; i += 3) {
		if (i == 0) {
		    xmin = xmax = ps[0];
		    ymin = ymax = ps[1];
		    zmin = zmax = ps[2];
		} else {
		    if (ps[i] < xmin) { 
			xmin = ps[i];
		    } else if (ps[i] > xmax) {
			xmax = ps[i];
		    }

		    if (ps[i + 1] < ymin) { 
			ymin = ps[i + 1];
		    } else if (ps[i + 1] > ymax) {
			ymax = ps[i + 1];
		    }

		    if (ps[i + 2] < zmin) { 
			zmin = ps[i + 2];
		    } else if (ps[i + 2] > zmax) {
			zmax = ps[i + 2];
		    }
		}
	    }
	}

	private float longestSide() {
	    float dx = xmax - xmin;
	    float dy = ymax - ymin;
	    float dz = zmax - zmin;

	    float max = dx;
	    if (dy > max) max = dy;
	    if (dz > max) max = dz;
	    return max;
	}

	float[] getCenter() {
	    float[] c = new float[3];
	    c[0] = (xmin + xmax)/2;
	    c[1] = (ymin + ymax)/2;
	    c[2] = (zmin + zmax)/2;
	    return c;
	}

	float[] getEye() {
	    float max = longestSide();
	    if (max < 0.1)
		max = 0.1f; // not too close
	    float[] c = getCenter();
	    return new float[] {c[0] + max * 0.5f, c[1] - max, c[2] + max * 0.5f};
	}

	void translateBy(float x, float y, float z) {
	    xmin += x;
	    xmax += x;
	    ymin += y;
	    ymax += y;
	    zmin += z;
	    zmax += z;
	}

	void scaleBy(float s) {
	    xmin *= s;
	    xmax *= s;
	    ymin *= s;
	    ymax *= s;
	    zmin *= s;
	    zmax *= s;
	}
    }
}


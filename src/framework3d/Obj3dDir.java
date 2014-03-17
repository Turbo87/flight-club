/*
 * @(#)Obj3dDir.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;
import java.io.*;
/**
   This class extends Obj3d to include a local frame of reference. We
   may rotate the local frame by specifying a unit vector v and an
   angle of bank. We may move the local frame by giving the co-ords of
   its origin.

   <p>We keep an extra list of points which represent the object's
   co-ords in its *local frame* of reference. We dictate that the
   object's initial co-ords are its local co-ords. 

   @see flightclub.client.MovingBody
*/
public class Obj3dDir extends Obj3d {
    private float[] locals; // local co-ords in a flattened array
    private float[] p = new float[3]; // position of the local origin
    private float[] i, j, k; // unit vectors along *local* x, y and z axes

    private static final float[] UP = new float[] {0, 0, 1};

    public Obj3dDir(ModelViewer modelViewer, int npolygons, boolean register) { 
	super(modelViewer, npolygons, register);
	init();
    }

    public Obj3dDir(ModelViewer modelViewer, int npolygons) { this(modelViewer, npolygons, true); }

    public Obj3dDir(StreamTokenizer st, ModelViewer modelViewer, boolean register) 
	throws IOException, FileFormatException {
	super(st, modelViewer, register);
	init();
    }

    /** 
	Creates a copy of an instance of Obj3dDir. The Glider uses
	this constructor; it is passed an instance of GliderType which
	holds a 3d object and clones that object so it has its own
	copy.  
    */
    public Obj3dDir(Obj3dDir from, boolean register) {
	super(from, register);
	init();
	System.arraycopy(from.ps, 0, locals, 0, npoints * 3);
    }

    /** Private helper fn for constructors. */
    private void init() {
	locals  = new float[maxpoints * 3];
	i = new float[] {1, 0, 0};
	j = new float[] {0, 1, 0};
	k = new float[] {0, 0, 1};
	i_ = new float[] {1, 0, 0};
	k_ = new float[] {0, 0, 1};
    }

    /**
       Extends super to also hold a copy of the initial co-ords as the
       local co-ords.  
    */
    int addPoint(float x, float y, float z) {
	int _npoints = npoints;
	int index = super.addPoint(x, y, z);

	// if we have added a new point we must copy it to locals
	if (npoints > _npoints) {

	    if (locals == null) {
		locals = new float[maxpoints * 3];
	    } else if (index >= locals.length) {
		//double array size
		float[] qs = new float[locals.length * 2];
		System.arraycopy(locals, 0, qs, 0, locals.length);
		locals = qs;
	    }

	    locals[index] = ps[index];
	    locals[index + 1] = ps[index + 1];
	    locals[index + 2] = ps[index + 2];
	}
	return index;
    }

    // state for setFrame
    private float[] _p = new float[3]; // previous p
    private float[] i_ = new float[3]; // like i but without any roll
    private float[] k_ = new float[3]; // like k but without any roll
    private float roll, _roll;
    private boolean dirtyR = false; // rotated frame
    private boolean dirtyT = false; // translated frame

    /**
      Sets the i, j and k vectors so that v points along the j and i
      and k are banked over. v should be a unit vector and the roll is
      in radians.
      
      <p>Looking along j (which equals v) towards origin we see the how the roll
      moves k away from the vertical and i away from the horizontal:

      <pre> 
                      k_    k
		      |   /
		      |* /         * = roll
		      | /
		      |/
		      .-----------> i_
		        \
			  \
			    \
			      i
      </pre>

      @see flightclub.client.MovingBody 
    */
    public void setFrame(float[] p, float[] v, float roll) {

	// any movement of origin since we last updated the co-ords
	if (p[0] == _p[0] && p[1] == _p[1] && p[2] == _p[2]) {
	    // local origin has not moved
	    // so lets look at rotation
	} else {
	    this.p[0] = p[0];
	    this.p[1] = p[1];
	    this.p[2] = p[2];
	    dirtyT = true;
	}

	// any rotation of axes ?
	if (roll == _roll && j[0] == v[0] && 
	    j[1] == v[1] && j[2] == v[2]) {
	    // no rotation - job done !
	    return;
	}

	j[0] = v[0];
	j[1] = v[1];
	j[2] = v[2];
	this.roll = roll;

	Tools3d.cross(v,UP,i_);
	Tools3d.cross(i_, j, k_);

	// now apply roll
	if (roll != 0) {
	    //Tools3d.linearSum(Tools3d.quickSin(roll), i_, Tools3d.quickCos(roll), k_, k);
	    //Tools3d.linearSum(Tools3d.quickCos(roll), i_, - Tools3d.quickSin(roll), k_, i);
	    Tools3d.linearSum((float) Math.sin(roll), i_, (float) Math.cos(roll), k_, k);
	    Tools3d.linearSum((float) Math.cos(roll), i_, (float) - Math.sin(roll), k_, i);
	} else {
	    k[0] = k_[0]; 
	    k[1] = k_[1]; 
	    k[2] = k_[2];
	    i[0] = i_[0];
	    i[1] = i_[1];
	    i[2] = i_[2];
	}
	dirtyR = true;
    }

    /** Updates the gloabl co-ords if the local frame has been
        rotated. This also handles any translation of the origin. */
    private void updateRotated() {

	for (int n = 0; n < npoints * 3; n += 3) {

	    // take the linear sum of local co-ords with local
	    // unit vectors and then translate by adding the local
	    // origin's co-ords
	    ps[n] = locals[n] * i[0] + locals[n + 1] * j[0] + locals[n + 2] * k[0];
	    ps[n + 1] = locals[n] * i[1] + locals[n + 1] * j[1] + locals[n + 2] * k[1];
	    ps[n + 2] = locals[n] * i[2] + locals[n + 1] * j[2] + locals[n + 2] * k[2];

	    ps[n] += p[0];
	    ps[n + 1] += p[1];
	    ps[n + 2] += p[2];
	}

	// reset bounding box and clear dirty flags
	box.setBB();
	dirtyR = false;
	dirtyT = false;

	// normals must be reset
	this.setNormals();

	// finally
	_p[0] = p[0];
	_p[1] = p[1];
	_p[2] = p[2];
	_roll = roll;
    }

    /** Updates the gloabl co-ords if the local frame has been
        translated. We have this seperate routine to handle the case
        where there has been a translation but no rotation *for
        speed*. Note that many moving bodies spend much of their time
        travelling in straight lines. */
    private void updateTranslated() {

	float[] dp = new float[3];
	Tools3d.subtract(p, _p, dp);

	for (int i = 0; i < npoints * 3; i+= 3) {
	    // translate by adding the amount local origin has moved by
	    ps[i] += dp[0];
	    ps[i + 1] += dp[1];
	    ps[i + 2] += dp[2];
	}
	
	// reset bounding box and clear dirty flag
	box.setBB();
	dirtyT = false;

	// finally
	_p[0] = p[0];
	_p[1] = p[1];
	_p[2] = p[2];
    }

    public void transform() {
	if (dirtyR) {
	    updateRotated();
	} else if (dirtyT) {
	    updateTranslated();
	}
	this.updateShadow();
	super.transform();
    }
}

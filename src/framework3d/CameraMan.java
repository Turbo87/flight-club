/*
 * @(#)CameraMan.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

import java.awt.*;
import java.awt.event.*;
/**
 * This class implements camera functionality such as cutting smoothly
 * between different camera subjects and mapping (x, y, z) onto the
 * screen (y_, z_)). The projection from (x, y, z) onto the screen
 * (y_, z_) is determined by two vectors, <code>eye</code> and
 * <code>focus</code>.
 *
 *<p>Using these two vectors we calculate the rotation and translation
 * needed to position the eye on the (+ve) x_ axis looking towards the
 * focus at origin_. Given a point (x, y, z) we translate it and apply
 * the rotation to get (x_, y_, z_). We then scale the y_ and z_ by
 * 1/x_ to complete the perspective projection by forshortening y_ and
 * z_ according to their distance from the camera.
 *
 * @author Dan Burton
 * @version 3.02, 27-jul-2002
 * @see ModelViewer#createCameraMan()
 * @see ModelCanvas#paintModel 
*/
public class CameraMan {
    protected ModelViewer modelViewer;
    protected float depthOfVision = 100;

    private float[] lightRay;
    private float distance = 0;
    private float[][] matrix;
    private int width, height;
    private float theScale;
    private float[] eye = {1.5f, 0, 0};
    private float[] focus = {0, 0, 0};
    private float[] ray = new float[3];
    private int rBackground=255;
    private int gBackground=255;
    private int bBackground=255;
    protected CameraSubject cameraSubject; 
    private int cutCount = 0;
    private float[] deye,dfocus;
    private float[] eyeGoto,focusGoto;
    private boolean track = false;
    protected boolean stayThere = false; // flag to stop camera from moving

    private static final float AMBIENT_LIGHT = (float) 0.5; //0.3
    private static final int CUT_RAMP = 12; //number of steps we accelerate over during a cut
    
    // shorten cut length while testing
    private static final int CUT_LEN = 75; //75; //nos of ticks to glide between camera subjects
 
    /**
      This field tells us how far away from a 1 unit tall object must
      the camera be in order for it to fill the height of the
      screen. Of course, we measure the lens angle in radians.  
    */
    protected float LENS_ANGLE_INVERSE = (float) 1.1;
    protected static final float ZOOM_STEP = (float) 0.25;
    /** 
     * Creates a CameraMan.
     *
     * @param modelViewer an instance of ModelViewer.
     */
    public CameraMan(ModelViewer modelViewer) {
	this.modelViewer = modelViewer;
	setMatrix();

	lightRay = new float[] {1, 1, -3};
	Tools3d.makeUnit(lightRay);
    }

    /**
     * Sets the scale according to the canvas height. Do not call
     * this method until the ModelCanvas knows how big it is.  
     */
    public void init() {
	width = modelViewer.modelCanvas.getSize().width;
	height = modelViewer.modelCanvas.getSize().height;
	theScale  = height * LENS_ANGLE_INVERSE;
    }

    /**
       Calcs how much light falls on a surface with this normal.

       <p>Return 0 if no light falls on surface and 1 if surface faces
       directly into the light. Usually we are somewhere in between.

       @param normal the unit normal vector.  
    */
    float surfaceLight(final float[] normal) {
	float dot = Tools3d.dot(lightRay, normal);
	dot = (- dot + 1)/2;  // Map the interval (-1, 1) to (0, 1).
	return (float) (AMBIENT_LIGHT + dot * (1 - AMBIENT_LIGHT));
    }

    /**
      Update camera position, only if we are mid cut
      or the subject is moving.

      @see ModelViewer#tick(float, float)
    */
    public void tick () {
	if (cameraSubject == null || stayThere) {
	    return;
	}
	if (cutCount > 0) cutStep();
	if (track) followSubject();
    }

    /**
       Allow for a moving subject - calc how much the subject has
       moved since last call and add that amount to eye and focus.
    */
    private void followSubject() {
	float[] eNew = cameraSubject.getEye();
	float[] fNew = cameraSubject.getFocus();
	float[] de = new float[3];
	float[] df = new float[3];

	Tools3d.subtract(eNew,eyeGoto,de);
	Tools3d.subtract(fNew,focusGoto,df);
	Tools3d.add(eye, de, eye);
	Tools3d.add(focus, df, focus);
	setMatrix();		
	
	//note subject's new position for next iteration
	eyeGoto = eNew;
	focusGoto = fNew;
    }

    /**
     * Sets the camera subject. The camera will gradually move to
     * its new position over CUT_LEN ticks.
     * 
     * @param cameraSubject the subject to film.
     * @param track if true then track the subject as it moves.
     */
    public void setSubject(CameraSubject cameraSubject, boolean track) {
	if (cameraSubject == null) return;

	this.cameraSubject = cameraSubject;
	this.track = track;

	cutCount = CUT_LEN;

	eyeGoto = cameraSubject.getEye();
	focusGoto = cameraSubject.getFocus();
		
	deye = new float[3];
	dfocus = new float[3];
		
	Tools3d.subtract(eyeGoto, eye, deye);
	Tools3d.subtract(focusGoto, focus, dfocus);

	/*
	  The eye accelerates from 0 upto velocity
	  deye over cutRamp steps, tracks at deye then
	  slows to zero over cutRamp steps. Similarly 
	  for focus.
	*/
	Tools3d.scaleBy(deye, (float) 1.0/(cutCount - CUT_RAMP));
	Tools3d.scaleBy(dfocus, (float) 1.0/(cutCount - CUT_RAMP));
    }

    /**
       Sets the camera subject. The camera moves immediately
       to the new position defined by the subject.
    */
    public void setSubjectNow(CameraSubject cameraSubject, boolean track) {
	this.cameraSubject = cameraSubject;
	this.track = track;

	cutCount = 0;
	eye = cameraSubject.getEye();
	focus = cameraSubject.getFocus();
	setMatrix();

	if (modelViewer.getDebug()) 
	    System.out.println(this.toString());

	eyeGoto = cameraSubject.getEye();
	focusGoto = cameraSubject.getFocus();
    }

    /*
      Iterate the cut. 
    */
    private void cutStep() {
	float[] deye_ = {deye[0], deye[1], deye[2]};
	float[] dfocus_ = {dfocus[0], dfocus[1], dfocus[2]};
	float s;
		
	if (cutCount > CUT_LEN - CUT_RAMP) {
	    //accelerating
	    s = (float) (CUT_LEN - cutCount)/CUT_RAMP;
	    Tools3d.scaleBy(deye_, s);
	    Tools3d.scaleBy(dfocus_, s);
	} else if (cutCount < CUT_RAMP) {
	    //decelerating
	    s = (float) cutCount/CUT_RAMP;
	    Tools3d.scaleBy(deye_, s);
	    Tools3d.scaleBy(dfocus_, s);
	} else {
	    //const speed - no need to scale
	}
		
	Tools3d.add(eye, deye_, eye);
	Tools3d.add(focus, dfocus_, focus);

	setMatrix();
	cutCount --;

	// debug
	if (cutCount == 0 && modelViewer.getDebug())
	    System.out.println(this.toString());
    }

    public float getDistance() { return distance; }
    public float[][] getMatrix() { return matrix; }
    public float[] getFocus() { return new float[] {focus[0], focus[1], focus[2]}; }
    public float[] getEye() { return new float[] {eye[0], eye[1], eye[2]}; }
    
    /**
      Rotates the eye about the focus by dtheta (L/R) 
      and moves the eye up or down by dz.
    */
    public void rotate(float dtheta,float dz) {

	/**
	   Transform ray.
	   TODO: optimize - dtheta only takes one of two vales - see ModelCanvas
	*/
	if (dtheta != 0) {
	    Tools3d.subtract(eye,focus,ray);
	    float[][] m = Tools3d.rotateX(new float[] {1, dtheta, 0 });
	    Tools3d.applyTo(m,ray,ray);
	    ray[2] += dz;		
	    Tools3d.add(ray,focus,eye);
	} else {
	    eye[2] += dz;
	}

	// TODO: move the following into a subclass 
	if (eye[2] < 0) eye[2] = 0;

	setMatrix();
    }

    /**
      Move focus whilst maintaining the position of the eye relative
      to the focus 
    */
    private void moveFocus(float[] f) {
	Tools3d.subtract(eye,focus,ray);
	focus[0] = f[0];
	focus[1] = f[1];
	focus[2] = f[2];
	Tools3d.add(ray,focus,eye);
    }
	
    /**
       Creates a rotation matrix such that eye is looking down +ve x
       axis towards the origin. Has the side effect of setting the var
       distance to how far the eye is from the focus. Call this method
       every time you move the eye or focus.

       @see Tools3d
    */
    private void setMatrix() {
	Tools3d.subtract(eye,focus,ray);
	matrix = Tools3d.rotateX(ray);
	distance = Tools3d.length(ray);
    }
	
    /**
       Scales the y_ and z_ co-ords so a 1 unit tall tree
       fills the screen height when viewed from a distance
       of LENS_ANGLE_INVERSE away.
			
       <p>Also translates y_ and z_ so that (x_, 0, 0)  appears center screen. 

       @see Obj3d#transform
    */
    public final void scaleToScreen(float[] v_) {
	v_[1] *= theScale; 
	v_[1] += width/2;
	v_[2] *= - theScale;
	v_[2] += height/2;
    }
	
    /**
       Mutes distant colors.

       <p>Remember x_ gives ~ distance of surface from camera
       (we are using the transformed coord x_); the
       camera is on the +ve x axis looking towards the 
       origin.
    */
    Color foggyColor(float x_, Color c) {

	if (x_ >= 0) return c;
	x_ *= -1;
	
	int r, g, b, r_, g_, b_;

	r = c.getRed();
	g = c.getGreen();
	b = c.getBlue();

	if (x_ > depthOfVision) {
	    r_ = rBackground;
	    g_ = gBackground;
	    b_ = bBackground;
	} else {
	    float f = x_/depthOfVision;
	    r_ = (int) (r + f * (rBackground - r));
	    g_ = (int) (g + f * (gBackground - g));
	    b_ = (int) (b + f * (bBackground - b));
	}

	return new Color(r_,g_,b_);
    }

    /** Move eye closer to the focus. */
    public void pullIn() { pull(-1); }

    /** Move eye further away from the focus. */
    public void pullOut() { pull(+1); }

    /** A private helper fn used by pullIn and pullOut. */
    private void pull(int dir) {
	distance *= (1 + dir * ZOOM_STEP);

	Tools3d.subtract(eye,focus,ray);
	Tools3d.makeUnit(ray);
	Tools3d.scaleBy(ray, distance);
	Tools3d.add(ray,focus,eye);

	this.setMatrix();

	if (modelViewer.getDebug())
	    System.out.println(this.toString());
    }

    /** Returns a string that may be handy for debugging. */
    public String toString() {
	return "Camera: \n\t distance: " + Tools3d.round(distance) + "\n\t eye: " + Tools3d.toString(eye) + "\n\t focus: " + Tools3d.toString(focus);
    }
}



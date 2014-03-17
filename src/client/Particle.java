/*
 * @(#)Particle.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.awt.Color;
/**
  This class implements a particle with a position and velocity. The
  particle travels in the direction given by the unit vector v at a
  specified speed. We have a factory method for giving the particle a
  tail. The particle may be made to follow a curved trajectory by
  calling makeTurn().   
*/
class Particle implements ClockObserver, CameraSubject {
    protected ModelViewer modelViewer;
    float[] p = new float[3];
    float[] v = new float[3];
    float nextTurn;
    float speed = 1;
    float turnRadius = 1;
    protected Tail tail = null;

    /** 
	Creates a particle that is at p travelling in direction
        v. Note that the speed and turn radius are both 1 until you
        set the values of speed and turnRadius. These two properties
        are protected so you must be a subclass to change their
        values.  

	@see Glider
    */
    public Particle(ModelViewer modelViewer, float[] p, float[] v) {
	this.modelViewer = modelViewer;
	modelViewer.clock.addObserver(this);

	this.p[0] = p[0];
	this.p[1] = p[1];
	this.p[2] = p[2];
	
	this.v[0] = v[0];
	this.v[1] = v[1];
	this.v[2] = v[2];
	Tools3d.makeUnit(v); 

	createTail();
    }

    /** Creates a particle at the origin travelling down the y axis.*/
    public Particle(ModelViewer modelViewer) {
	this(modelViewer, new float[] {0, 0, 0}, new float[] {0, 1, 0});
    }

    /** 
	Sets the particles position.

	<p>This method is only used by the GliderNetworked which gets p and
	v from the game server. 

	@see GliderNetworked
    */
    void tweakPosition(float x, float y, float z) {
	p[0] = x;
	p[1] = y;
	p[2] = z;
    }

    /** 
	Sets the velocity vector. Well, we set its direction. The
	length is set to 1 because the variable *speed* determines how
	far the particle travels per unit time.

	<p>This method is only used by the GliderNetworked which gets p and
	v from the game server. 
	
	@see GliderNetworked 
    */
    void tweakVelocity(float vx, float vy, float vz) {
	v[0] = vx;
	v[1] = vy;
	v[2] = vz;

	// TODO: ensure that GliderNetworked only netSends unit
	// vectors, then we can drop the next line...
	Tools3d.makeUnit(v); 
    }
	
    protected void createTail() {
	tail = new Tail(modelViewer, this, Color.lightGray);
	tail.init();
    }
	
    /** 
	Updates the particle's position, velocity and tail. The only
        change to v (if any) will be a rotation of v about the z axis. 
    */
    public void tick(float t, float dt) {

	// direction
	if (nextTurn != 0) {
	    makeTurn(dt);
	    // we do *not* clear nextTurn - the controller does that
	}

	// distance equals speed * time
	float ds = speed * dt;

	// move ! (remember that v is a unit vector)
	p[0] = p[0] + ds * v[0];
	p[1] = p[1] + ds * v[1];
	p[2] = p[2] + ds * v[2];

	if (tail != null) {
	    tail.tick(t, dt);
	}
    }

	
    /**
       Makes me turn to left (-ve) or right (+ve). We ignore the
       vertical component of the motion and simply work in the xy
       plane. This seperation of xy from z works pretty well for
       Flight Club where we use Particle to build, for example,
       gliders, jets, roads, balloons...
			
       <p>The argument <dir> works as follows. The further from zero the
       tighter the turn.  > 0 turn right, < 0 turn left, 1 - my turn
       radius 2 - halve my turn radius etc...  

       All the work is actually done in another private method,
       makeTurn, which gets called each tick.  
    */
    public void setTurn(float dir) {
	nextTurn = dir;
    }

    // some state for makeTurn()
    private float sH; // horizontal speed
    private float _vx, _vy; // previous values of v[0] and v[1]
    private float[][] m; // a rotation matrix
    private float _angle; 

    /**
       Rotates v about the z axis. This routine is called from
       <code>tick</code> if <code>nextTurn</code> has a non zero
       value.  
    */
    private void makeTurn(float dt) {
	if (v[0] != _vx || v[1] != _vy) {
	    sH = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
	    _vx = v[0];
	    _vy = v[1];
	}

	if (sH == 0) return;

	float angle = sH * dt/turnRadius;
	angle *= nextTurn;

	if (angle != _angle) {
	    m = Tools3d.rotateAboutZ(angle);
	    _angle = angle;
	}

	Tools3d.applyTo(m, v, v);
    }

    public float[] getFocus() {
	return new float[] {p[0], p[1], p[2]};
    }

    public float[] getEye() {
	return new float[] {p[0] + turnRadius, p[1] - turnRadius * 2, p[2] + turnRadius * 0.2f};
    }

    public void destroyMe() {
	if (tail != null) tail.destroyMe();
	modelViewer.clock.removeObserver(this);
    }

    // see MovementManager - for speed we don't want to calculate sH
    // unless we have to.
    float getHorizontalSpeed() {
	if (sH != 0) {
	    return sH;
	} else {
	    sH = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
	    return sH;
	}
    }
}

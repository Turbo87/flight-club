/*
 * @(#)GliderAI.java (part of 'Flight Club')
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
  This class implements an AI glider. The glider will sniff out lift,
  climb to base then fly downwind in search of more lift.

  TODO: When to climb and when to glide to a *better* climb ? Also, a
  rule that if h < h1 then be *cautious* ?
  
  TODO: Different AI profiles: eg. racer vs floater
*/
public class GliderAI extends GliderTask {
    MovementManager moveManager;
    private boolean tryLater = false; // no lift found yet so glide for a bit on track

    // vars for delayed camera cuts
    boolean cutPending = false;
    float cutWhen = 0;
    CameraSubject cutSubject = null;

    // search in a sector this far either side of being on track
    final double searchSector = Math.PI/4;  

    // if a search for lift fails then try again after this much time
    private static final float T_LATER = 1.0f; 
    //static final Color[] colors = {Color.PINK, Color.ORANGE, Color.GREEN, Color.RED, Color.BLUE};

    public GliderAI(XCModelViewer xcModelViewer, GliderType gliderType, int id) { 
	super(xcModelViewer, gliderType, id);
	moveManager = new MovementManager(xcModelViewer, this);
	//this.obj.setColor(colors[myID%colors.length]);
    }

    /**
       Start flying the task.
     */
    public void takeOff() {
	super.takeOff();
	makeDecision(xcModelViewer.clock.getTime());
    }

    // time of last search for lift (an expensive operation)
    private float t_ = 0;

    /**
       Searches my node for the next lift source.
    */
    void makeDecision(float t) {
	Node[] nodes = xcModelViewer.xcModel.task.nodeManager.loadedNodes();
	LiftSource ls = null;
	for (int i = 0; i < nodes.length; i++) {
	    ls = nodes[i].search(this);
	    if (ls != null) {
		break;
	    }
	}
	t_ = t; // note time of search

	if (ls == null) { // glide torwards next turn point
	    this.moveManager.setTargetPoint(new float[] {nextTP.x_, nextTP.y_, 0}, true);
	    tryLater = true;
	    return;
	}

	// glide to lift source - a cloud or hill
	try {
	    Cloud cloud = (Cloud) ls;
	    this.moveManager.setCloud(cloud);
	    if (filmID == myID) {
		cutPending = true;
		cutWhen = t + whenArrive(cloud.x, cloud.y) - 2; 
		cutSubject = (CameraSubject) cloud;
	    }
	} catch (Exception e) {;}

	try {
	    Hill hill = (Hill) ls;
	    this.moveManager.setCircuit(hill.getCircuit());
	} catch (Exception e) {;}
    }	

    /**
       Is there a better lift source within reach ?
    */
    void reviewDecision(float t) {
	Node[] nodes = xcModelViewer.xcModel.task.nodeManager.loadedNodes();
	LiftSource ls = null;
	for (int i = 0; i < nodes.length; i++) {
	    ls = nodes[i].search(this);
	    if (ls != null) {
		break;
	    }
	}
	t_ = t; // note time of search

	if (ls == null) {
	    // stick to last decision
	    return;
	}

	// glide to cloud if it is *stronger*
	try {
	    Cloud cloud = (Cloud) ls;
	    if (moveManager.cloud == null || cloud.getLift() > moveManager.cloud.getLift()) {
		this.moveManager.setCloud(cloud);
		if (filmID == myID) {
		    cutPending = true;
		    cutWhen = t + whenArrive(cloud.x, cloud.y) - 2;
		    cutSubject = (CameraSubject) cloud;
		}
	    }
	} catch (Exception e) {;}

	try {
	    Hill hill = (Hill) ls;
	    this.moveManager.setCircuit(hill.getCircuit());
	} catch (Exception e) {;}
    }	

    public void tick(float t, float dt) {
	if (!landed) {
	    nextTurn = moveManager.nextMove();
	    tickAI(t);
	}
	super.tick(t, dt);
    }

    private boolean finalGlide = false; // set to true when final gliding
    static final float T_THINK = 1.0f; // don't think too hard (CPU)
    /**
       Checks for certain conditions. Eg. have i reached base ? 

       TODO: A better design would not keep checking every tick, but
       use a call back ?
    */
    protected void tickAI(float t) {
	// time for a camera move ?
        if (cutPending) {
            if (t >= cutWhen) {
		cutNow();
	    }
        }

	if (finalGlide) {
	    return;
	}

	// final glide
	if (nextTP.nextTP == null) {
	    if (withinGlide(nextTP.x, nextTP.y)) {
		this.moveManager.setTargetPoint(new float[] {nextTP.x_, nextTP.y_, 0}, true);
		finalGlide = true;
	    }
	}

	// am i thermalling under a decaying cloud ?
	if (moveManager.cloud != null && moveManager.cloud.lifeCycle.isDecaying()) {
	    if (Glider.filmID == myID) {
		modelViewer.cameraMan.setSubject(this, true);
	    }
	    makeDecision(t);
	    return;
	}

	// am i at base ?
	if (this.obj.getZmax() >= Cloud.CLOUDBASE && t > t_ + T_LATER) {
	    if (Glider.filmID == myID) {
		modelViewer.cameraMan.setSubject(this, true);
	    }
	    makeDecision(t);
	    return;
	}

	// am i gliding and waiting 
	if (tryLater) {
	    if (t > t_ + T_LATER) {
		tryLater = false;
		makeDecision(t);
	    }
	    return;
	}

	// otherwise, review me last decision every so often - perhaps
	// we can now reach a better climb ?
	if (t > t_ + T_THINK) {
	    reviewDecision(t);	    
	}
    }


    protected void reachedTurnPoint() {
	makeDecision(xcModelViewer.clock.getTime());
    }

    /**
       Returns true if (x, y) is within glide. Adding wind makes this
       a bit fiddly. The glide angle will be reduced by any head wind
       etc.
    */
    private boolean withinGlide(float x, float y) {
	float[] u = this.onTrack(); 
	float s = this.getSpeed();
	u[0] *= s;
	u[1] *= s;
	u[0] += air[0];
	u[1] += air[1];
	float s_ = (float) Math.sqrt(u[0] * u[0] + u[1] * u[1]);
	float glideAngle_ = s_/- this.getSink();

	float[] r = new float[3];
	Tools3d.subtract(new float[] {x, y, p[2]}, p, r);
	float d = Tools3d.length(r);
	return d <= p[2] * glideAngle_;
    }



    /**
       Returns a unit vector pointing in the direction we want to
       fly. Note we glide to (x_, y_), a point inside the turn point
       sector rather than (x, y) itself.

       Todo: take into account the wind exactly (need to solve a
       quadratic eq). I have an approx solution which is ok for light
       winds.
    */
    float[] onTrack() {
	float[] r = new float[3];
	Tools3d.subtract(new float[] {nextTP.x_, nextTP.y_, p[2]}, p, r);

	// time to get to next turn point assuming nil wind
	float t = Tools3d.length(r)/this.getSpeed();

	// drift due to wind during this amount of time
	float[] drift = new float[] {air[0] * t, air[1] * t, 0};

	// offset turn point by - drift to get ~desired heading
	Tools3d.subtract(new float[] {nextTP.x_ - drift[0], nextTP.y_ - drift[1], p[2]}, p, r);
	Tools3d.makeUnit(r);
	return r;
    }


    /**
       How long to get to a cloud. Note we may ignore the wind as both
       the cloud and the glider experience the same drift.
    */
    float whenArrive(float x, float y) {
        float d = (p[0] - x) * (p[0] - x) + (p[1] - y) * (p[1] - y);
        d = (float) Math.sqrt(d);
        return d / this.getSpeed();
    }

    /**
       Makes a camera cut. This is a bit fiddly - we wait until the
       glider is close to the hill/thermal so the camera does not get
       ahead of its subject.
    */
    private void cutNow() {
	if (Glider.filmID == myID) {
	    modelViewer.cameraMan.setSubject(cutSubject, true);
	}
        
        cutPending = false;
        cutSubject = null;
    }
}


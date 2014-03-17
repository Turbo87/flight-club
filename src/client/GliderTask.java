/*
 * @(#)GliderTask.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
/**
   Adds next turn point to glider state. This class contains stuff
   common to GliderUser and GliderAI.
*/
public class GliderTask extends Glider {
    protected TurnPoint nextTP = null; // the next turn point

    public GliderTask(XCModelViewer xcModelViewer, GliderType gliderType, int id) { 
	super(xcModelViewer, gliderType, id);
	nextTP = xcModelViewer.xcModel.task.turnPointManager.turnPoints[1];
    }

    /**
       Start flying the task.
     */
    public void takeOff() {
	super.takeOff();
	nextTP = xcModelViewer.xcModel.task.turnPointManager.turnPoints[1];
    }

    public void tick(float t, float dt) {
	super.tick(t, dt);
	checkSector(t);
    }

    /**
       Am i in sector ?
    */
    private void checkSector(float t) {
	if (nextTP == null) return; 
	if (nextTP.inSector(this.p)) {
	    if (nextTP.nextTP != null) {
		nextTP = nextTP.nextTP;
		reachedTurnPoint();
	    } else {
		finishedTask();
	    }
	    if (Glider.filmID == myID) {
		modelViewer.cameraMan.setSubject(this, true);
	    }
	}
    }

    protected void reachedTurnPoint() {;}

    private boolean finished = false;
    private float timeFinished = 0;

    private void finishedTask() {
	finished = true;
	timeFinished = timeFlying;
    }

    /**
       Returns distance flown around the task. We take the distance of
       the last turn point, A, from the task start and add the
       distance that we are away from A in the direction of the next
       turn point, B.
    */
    private float distanceFlown() {
	TurnPoint tp = nextTP.prevTP;
	float[] r = new float[3];
	Tools3d.subtract(p, new float[] {tp.x, tp.y, p[2]}, r);
	float d2 = Tools3d.dot(r, new float[] {tp.dx, tp.dy, 0});
	return d2 + tp.distanceFromStart;
    }

    /**
       Returns a text message giving current state of glider. Note the
       formatting of model units for human consumption...

       time: divide by 2 gives minutes
       distance: divide by 2 gives kilometers
    */
    public String getStatusMsg() {
	String times = "T1: " + (int) timeFlying + "  T2: " + (int) xcModelViewer.clock.getTime();

	if (finished) {
	    return "You have reached goal !  " + times;
	} else if (landed) {
	    return "Landed ! D: " + (int)(this.distanceFlown()/2) + "km  " + times;
	} else {
	    return "D: " + (int)(this.distanceFlown()/2) 
		+ "km  H: " + (int) ((p[2]/3) * 1500) + "m  " + times;
	}
    }

    static final float EYE_D = 2; //3.0f; //2
    static final float EYE_H = 0.3f;
    static final float FOCUS_D = 1.0f;
    /**
       Sets the camera focus and eye. The glider should not sit at the
       center of the screen. Rather...
       
       glider -> screen
      
       high -> top
       left of track -> left
       right of track -> right
       low -> bottom
    */

    public float[] getFocus() {
	float z;
        if (p[2] < 0.5) {
	    z = 0.5f;
	} else {
	    z = 0.5f + (p[2] - 0.5f) * 0.6f; //0.5f
	}
	TurnPoint tp = nextTP.prevTP;
	return new float[] {p[0] + tp.dx * FOCUS_D, p[1] + tp.dy * FOCUS_D, z};
    }

    public float[] getEye() {
	TurnPoint tp = nextTP.prevTP;
	float z = p[2] + EYE_H;
	if (z > Cloud.CLOUDBASE - 0.5f) z = Cloud.CLOUDBASE - 0.5f;

	return new float[] {p[0] - tp.dx * EYE_D, 
			    p[1] - tp.dy * EYE_D, 
			    z};
	//return new float[] {p[0], p[1], p[2]};
	//return new float[] {p[0] - v[0] * EYE_D, p[1] - v[1] * EYE_D, p[2] - v[2] * EYE_D}; 
    }

    void asString() {
	System.out.println("GliderTask(" + myID + "): p="
			   + Tools3d.asString(p)
			   + ", nextTP=" + nextTP.myID);
    }

}

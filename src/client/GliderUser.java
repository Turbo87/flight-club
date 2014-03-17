/*
 * @(#)GliderUser.java (part of 'Flight Club')
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
import java.awt.event.*;

/**
   This class implements a glider that is controlled by *the* user.  
*/
public class GliderUser extends GliderTask implements EventInterface {
    Variometer vario;
    int cameraMode = XCCameraMan.USER;

    public GliderUser(XCModelViewer xcModelViewer, GliderType gliderType, int id) { 
	super(xcModelViewer, gliderType, id);
	modelViewer.eventManager.addNotification(this); // i want to hear about keyboard events
	//this.obj.setColor(new Color(255, 200, 0)); // orange
	vario = new Variometer(xcModelViewer, this);
    }

    protected void createTail() {
	tail = new Tail(modelViewer, this, new Color(255, 200, 0), Tail.NUM_WIRES * 2, Tail.WIRE_EVERY);
	tail.init();
    }

    public void destroyMe() {
	super.destroyMe();
	modelViewer.eventManager.removeNotification(this);
    }

    public void tick(float t, float dt) {
	super.tick(t, dt);
	if (!landed) {
	    vario.tick(t);
	    netSend();
	}
    }

    static final int PG_STYLE = 0;
    static final int SP_STYLE = 1;
    private int controlStyle = PG_STYLE;
    /**
       May choose between two styles of control...

       1. Paraglider style control. Turn whilst key is held down. Release
       key to fly straight on again. This is the old 3.01 style.

       2. Sailplane style control. Dab stick right causes glider to start
       turning right. Glider continues turning until pilot dabs stick
       left. No need to hold a key down !

       Note: Problem with java on linux - the key auto repeat
       generates lots of extra keypress and release events when user
       holds down a key. So on linux use sailplane style of
       control ?
    */
    public void keyPressed(KeyEvent e) {
	if (landed) {
	    return;
	}

	int key = e.getKeyCode();
        switch (key) {
        case 37://left arrow
	    if (controlStyle == PG_STYLE) {
		setMove(-1);
	    } else {
		incMove(-1);
	    }
	    if (((XCCameraMan)modelViewer.cameraMan).mode == XCCameraMan.USER) {
		modelViewer.cameraMan.setSubject(this, true);
	    }
            break;
        case 39://right arrow
	    if (controlStyle == PG_STYLE) {
		setMove(1);
	    } else {
		incMove(1);
	    }
	    if (((XCCameraMan)modelViewer.cameraMan).mode == XCCameraMan.USER) {
		modelViewer.cameraMan.setSubject(this, true);
	    }
            break;
        case 32://space
	    setPolar(0); // slowest
	    setMove(-1); // circle left
            break;
        case 38://up arrow
        case 38-32:
	    setMove(0); // onwards
            goFaster();
            break;
        case 40://down arrow
        case 40-32:
	    setMove(0); // onwards
            goSlower();
            break;
        default:
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
        case 37://left arrow
	    if (controlStyle == PG_STYLE) {
		setMove(0);
	    }
            break;
        case 39://right arrow
	    if (controlStyle == PG_STYLE) {
		setMove(0);
	    }
            break;
        default:
        }
    }

    private void setMove(int move) { nextTurn = move; }

    private void incMove(int move) {
	nextTurn += move; 
	if (nextTurn > 1) nextTurn = 1;
	if (nextTurn < -1) nextTurn = -1;
    }


    private int _nextTurn = 0;
    private int _iP = 0;
    /**
       Send to server the values of p, vx, vy, iP and the next
       move. We only do this if either nextTurn has changed or iP has
       changed since this fn was last called.
    */
    private void netSend() { 
	if (nextTurn != _nextTurn || iP != _iP) {
	    if (xcModelViewer.xcNet != null) {
		xcModelViewer.xcNet.send("#" + 
					     round(this.p[0]) + ":" + 
					     round(this.p[1]) + ":" + 
					     round(this.p[2]) + ":" + 
					     round(this.v[0]) + ":" + 
					     round(this.v[1]) + ":" +
					     this.iP + ":" + 
					     (int) nextTurn);
		_nextTurn = (int) nextTurn;
		_iP = iP;
	    }
	}
    }

    /** Round to floats to n decimal places. */
    private float round(float x) {
	return Math.round(x * 10000f)/10000f;
    }

    void hitTheSpuds() {
	super.hitTheSpuds();
	if (xcModelViewer.xcNet != null) {
	    xcModelViewer.xcNet.send("Landed");
	}
    }

    public void takeOff() {
	super.takeOff();
	if (xcModelViewer.xcNet != null) {
	    xcModelViewer.xcNet.send("Launched: " + this.typeID);
	}
    }

    public float[] getFocus() {
	if (cameraMode == XCCameraMan.USER) {
	    return super.getFocus();
	} else {
	    TurnPoint tp = nextTP.prevTP;
	    return new float[] {p[0] + v[0] * EYE_D, p[1] + v[1] * EYE_D, p[2] + v[2] * EYE_D}; 
	}
    }

    private final float EYE_DD = 0.1f;
    
    public float[] getEye() {
	if (cameraMode == XCCameraMan.USER) {
	    return super.getEye();
	} else {
	    TurnPoint tp = nextTP.prevTP;
	    return new float[] {p[0] + v[0] * EYE_DD, p[1] + v[1] * EYE_DD, p[2] + v[2] * EYE_DD}; 
	}
    }

    /**
       Allows camera man to either follow the glider from behind or look
       from the point of view of the pilot.
    */
    void setCameraMode(int mode) { cameraMode = mode; }
}

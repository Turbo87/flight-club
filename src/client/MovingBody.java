/*
 * @(#)MovingBody.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
/**
   This class extends Particle by adding a rigid body which has its
   own local frame. The local frame (i, j, k) will pitch, roll and yaw
   as the particle moves. This is the class we use to build gliders,
   cars, jets, birds...  
*/
abstract class MovingBody extends Particle {
    Obj3dDir obj;
    private float roll = 0;
    private float rollRate = 1;
    private float rollMax = (float) (Math.PI/4);

    public MovingBody(ModelViewer modelViewer, Obj3dDir obj) {
	super(modelViewer);
	this.obj = obj;
    }

    /** 
	Rolls to left or right or returns to level if not currently
	turning). We set the roll rate such that after turning thru' 1
	radian the roll is at its max value.  
    */
    private void updateRoll(float dt) {

	rollRate = (speed/turnRadius) * rollMax;

	// special case if speed is zero - glider landed but still has
	// to roll level !
	if (speed == 0) {
	    rollRate = 1/1 * rollMax;
	}

	if (nextTurn != 0) {

	    // turning
	    roll += nextTurn * rollRate * dt;

	    if (roll > rollMax) roll = rollMax;
	    if (roll < - rollMax) roll = - rollMax;

	} else if (roll != 0) {

	    // roll back towards level
	    float droll = rollRate * dt;

	    if (roll > droll) {
		roll -= droll;
	    } else if (roll < -droll) { 
		roll += droll;
	    } else {
		roll = 0;
	    }
	}
    }

    public void tick(float t, float dt) {

	// roll
	updateRoll(dt);

	// p and v
	super.tick(t, dt);
	
	// pass message to obj
	obj.setFrame(p, v, roll);
    }

    public void destroyMe() {
	modelViewer.obj3dManager.removeObj(this.obj);
	super.destroyMe();
    }
}

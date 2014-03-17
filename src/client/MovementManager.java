/*
 * @(#)MovementManager.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.awt.*;
import java.util.*;
/**
   This class manages the motion of particles - thermalling, ridge
   soaring etc.
*/
class  MovementManager {
    XCModelViewer xcModelViewer;
    Particle particle = null;	
	
    private float[] targetPoint = null;	//point to fly towards
    boolean grounded = false; //is target point on ground or in air ?
    float[] air = new float[] {0, 0, 0}; // wind
    private float[] circlePoint = null;	//point to circle around
    Cloud cloud = null;	//cloud to thermal
    private float[] targetDirection = null;	//direction to fly 
    private Circuit circuit = null;	//list of points to fly round
    private float[] circuitPoint = null;
    private int avoidHillCount = 0;

    private float speedOverHypot;
    int nextMoveUser = 0; 
    float lastDistance = 0;
    boolean joinedCircuit = false;
    boolean finishedCircuit = false;
    boolean user = false; //hacky flag for user input

    int wiggleCount = 0;
    int wiggleSize = 5;

    static final int LEFT = -1;
    static final int STRAIGHT = 0;
    static final int RIGHT = 1;
	
    static final int CIRCLE_DIR = LEFT;
    static final float E_COS = (float) 0.05; 
    static final float E_DIST = (float) 0.1; 
	
    public MovementManager(XCModelViewer xcModelViewer, Particle particle) {
	this.xcModelViewer = xcModelViewer;
	this.particle = particle;
	Task task = xcModelViewer.xcModel.task; 
	if (task != null) { // roads created before we have a task object
	    this.air[0] = task.wind_x;
	    this.air[1] = task.wind_y;
	}
    }
	
    private float wiggle() {
	wiggleCount--;
	if (wiggleCount > wiggleSize * 3) {
	    return -2;
	}
	if (wiggleCount > wiggleSize * 2) {
	    return 2;
	}
	if (wiggleCount > wiggleSize * 1) {
	    return 2;
	}
	if (wiggleCount > wiggleSize * 0) {
	    return -2;
	}
	return 0;
    }
	
    float nextMove() {
	/*
	  called by the particle each tick - turn left (-1)
	  right (+1) or straight on (0)
	*/

	if (wiggleCount > 0) {
	    return wiggle();
	}

	if (user) {
	    return nextMoveUser;
	}

	/*
	  TODO: do NOT overload nextMoveUser !

	  only use nextMoveUser for following a circuit - a kludge to
	  get turning in a certain direction after reaching a circuit
	  point. Note we clear the value as soon as we use it.
	*/
	if (avoidHillCount > 0) {
	    avoidHillCount--;
	    return nextMoveUser;
	}
		
	if (targetPoint != null) {
	    return headForTarget();
	}

	if (circuit != null) {
	    return followCircuit();
	}

	if (cloud != null) {
	    return thermal();
	}
		
	if (circlePoint != null) {
	    return circleAroundPoint();
	}

	//otherwise fly straight on
	return 0;
    }
	
    void setCircuit(Circuit circuit) {
	clearControllers();	
	this.circuit = circuit;
	joinedCircuit = false;
	finishedCircuit = false;
	circuitPoint = circuit.next();
	grounded = true;
    }
	
    void setTargetPoint(float[] t, boolean grounded) {
	clearControllers();
	targetPoint = new float[] {t[0], t[1], t[2]};
	this.grounded = grounded;
    }
	
    void setCirclePoint(float[] c) {
	//take a copy of c (otherwise it may move eg. particle.p)
	clearControllers();
	circlePoint = new float[] {c[0], c[1], c[2]};
	grounded = true;
    }

    // TODO: move to subclass ?
    void setCloud(Cloud c) {
	clearControllers();
	cloud = c;
	grounded = false;
    }
	
    void setTargetDirection(float[] d) {
	clearControllers();
	targetDirection = d;
    }
	
	
    void clearControllers() {
	targetDirection = null;
	circlePoint = null;
	targetPoint = null;
	cloud = null;
	circuit = null;
	joinedCircuit = false;
	user = false;
    }
	
    float[] getCirclePoint() {
	return circlePoint;
    }
	
    Circuit getCircuit() {
	return circuit;
    }
	
    boolean joinedCircuit() {
	if (circuit == null) {
	    return false;
	} else {
	    //have we gone round first point yet
	    return joinedCircuit;
	}
    }
	
    float headForTarget() {
	return headTowards(targetPoint[0],targetPoint[1]);
    }

    private float followCircuit() {
	float x = circuitPoint[0];
	float y = circuitPoint[1];

	//hack - the circuit should do this leaning calc!
	//use fall line to calc change due to height
	x += particle.p[2] * circuit.fallLine[0];
	y += particle.p[2] * circuit.fallLine[1];
	
	return headTowards(x,y);
    }
	
    /**
       Uses the cross product to see if we need to turn left or right.
    */
    private float headTowards(float x, float y) {
	float ds = particle.getHorizontalSpeed();
	float r = particle.turnRadius;
	float[] p = particle.p;
	float[] u;
	if (!grounded) { // ignore wind
	    u = new float[] {x - p[0], y - p[1], 0};
	} else { //offset target point due to wind
	    float[] u_ = new float[] {x - particle.p[0], y - particle.p[1], 0};
	    float t = Tools3d.length(u_)/ds;
	    float[] drift = new float[] {air[0] * t, air[1] * t, 0};
	    u = new float[] {x - drift[0] - p[0], y - drift[1] - p[1], 0};
	}
	float[] v = new float[] {particle.v[0], particle.v[1], 0};
	float d = Tools3d.length(u);
		
	if (d < r/4) {
	    //we are there
	    targetPoint = null;
	    if (circuit != null) reachedCircuitPoint();			
	    return 0;
	}

	// are we flying ~ staight towards target ?
	float dot = Tools3d.dot(u, v)/(ds * d); 
	if (dot > 0.99) {
	    return 0;
	}
		
	float[] c = new float[3];
	Tools3d.cross(v, u, c);
	float sin = Tools3d.length(c)/(ds * d);
	 // bug - forgot dt !
	float dt = 1.0f/xcModelViewer.clock.getFrameRate();
	float sin1 = ds * dt/r; // amount turned in dt at characteristic turn radius
		
	// are we heading in almost the right direction
	if ( sin <= sin1 * 2 && sin >= - sin1 * 2) {
	    /*
	      maintain ~ current heading (with
	      a bit of fine tuning to eliminate wobble)
	      eq1: ds = r * dtheta
	      eq2: dtheta ~ sin(dtheta) ( for small dtheta )
	    */
	    if (c[2] > 0) sin *= -1;	//left
	    float rr = ds/sin;
	    return r/rr; // scale so that eg. 1/2 my turn radius goes to 2
	}
		
	// Turn left of right depending whether cross product was -ve
	// or +ve. Hack to make turns twice as tight if particle is
	// following a circuit.
	if (c[2] > 0) {
	    // left
	    if (circuit == null) return -1; else return -2; //2
	} else {
	    // right
	    if (circuit == null) return 1; else return 2; //2 
	}
    }

    void reachedCircuitPoint() {
	if (circuit.avoidHill) {
	    // force glider to turn away from the hill
	    nextMoveUser = circuit.turnDir();  
	    avoidHillCount = 10;
	}
	joinedCircuit = true;
	circuitPoint = circuit.next();
	if (circuitPoint == null) {
	    // completed circuit - stop !
	    circuit = null;
	    finishedCircuit = true;
	    // xcModelViewer.clock.removeObserver(this.particle);
	}
    }
	
    private float circleAroundPoint() {return circleAround(circlePoint[0], circlePoint[1]);}

    /**
       Uses cross product of v and r
    */
    private float circleAround(float x, float y) {
	float[] r = new float[] {particle.p[0] - x, particle.p[1] - y, 0};
	float d = Tools3d.length(r);
	float tR = particle.turnRadius;
	float ds = particle.getHorizontalSpeed();

	// are we close ? if not then head towards circle centre
	if (d > tR * 3) {
	    return headTowards(x, y);
	}
		
	float[] cross = new float[3];
	Tools3d.cross(r, particle.v, cross);

	float dperp = Tools3d.length(cross)/ds;
	float dot = Tools3d.dot(r, particle.v);
		
	if (cross[2] >= 0) {
	    //circling the right way
	    if (dot > 0) {
		return -1;
	    } else {
		if (dperp <= tR) {
		    return 0; //was 0 on 26th whem it worked
		} else  {
		    return -1;
		}
	    }
	} else {
	    //circling the wrong way
	    if (d < tR) {
		return -1; 
	    } else {
		if (dot > 0) {
		    return -1; 
		} else {
		    return 1;
		} 
	    }
	}
    }
	
    void toggleCirclePoint() {
	if (circlePoint == null) {
	    setCirclePoint(particle.p); 
	} else {
	    circlePoint =null;
	}
    }

    float thermal() {
	return circleAround(cloud.getX(particle.p[2]), cloud.getY(particle.p[2]));
    }

    boolean workingLift() {
	return (cloud != null||circuit != null);
    }

    void doWiggle() {
	wiggleCount = wiggleSize * 4 + 1;
    }

    /** Sailplane style control. */
    void incMoveUser(int dir) {
	clearControllers();
	user = true;
	nextMoveUser += dir;
	if (nextMoveUser > 1) nextMoveUser = 1;
	if (nextMoveUser < -1) nextMoveUser = -1;
    }

    /** Paraglider style control. */
    void setMoveUser(int dir) {
	clearControllers();
	user = true;
	nextMoveUser = dir;
    }

}

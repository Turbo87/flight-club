package org.flightclub;/*
  FlyingDot.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Dan Burton , Nov 2001 
*/

import java.awt.*;

/*
  a dot with a position and velocity. also...
  - a local coord system (v points along +y, z has roll effect)
  - turn radius to use when circling. halve this for ridge sooaring.
  - factory method for a tail
  - ds (horizontal distance moved per tick)
*/

class FlyingDot implements ClockObserver, CameraSubject {
    final ModelViewer app;
    Vector3d v;
    Vector3d p = new Vector3d();
    float speed;
    float ds;//distance per frame - hack
    final float my_turn_radius;

    boolean isUser = false; //TODO - tidy this hack ! classname operator ?

    final Vector3d axisX = new Vector3d();
    final Vector3d axisY = new Vector3d();
    final Vector3d axisZ = new Vector3d();

    Tail tail = null;
    MovementManager moveManager = null;

    int roll = 0;
    static final int ROLL_STEPS = 15;
    static final float ROLL_MAX_ANGLE = (float) (Math.PI / 4);
    static Vector3d[] axisZs;

    public FlyingDot(ModelViewer theApp, float inSpeed, float inTurnRadius) {
        app = theApp;
        app.clock.addObserver(this);

        speed = inSpeed;
        ds = speed * app.timePerFrame;
        v = new Vector3d(0, ds, 0);
        my_turn_radius = inTurnRadius;
    }

    public FlyingDot(ModelViewer theApp, float inSpeed, float inTurnRadius, boolean inIsUser) {
        this(theApp, inSpeed, inTurnRadius);
        isUser = inIsUser;
    }

    public void init(Vector3d inP) {
        p = new Vector3d(inP.x, inP.y, inP.z);
        initRoll();
        setLocalFrame();
        createTail();
        createMoveManager();
    }

    protected void createTail() {
        int tailLength = 25;
        tail = new Tail(app, tailLength, Color.lightGray);
        tail.init(p);
    }

    void setSpeed(float s) {
        //call eg if user moved to faster point on the polar
        speed = s;
        ds = speed * app.timePerFrame;
    }

    protected void createMoveManager() {
        moveManager = new MovementManager(app, this);
    }

    public void tick(Clock c) {
    /*
	  update position, velocity and local frame
	*/
        Tools3d.add(p, v, p);
        p.y += Sky.getWind() * app.timePerFrame;
        ds = speed * app.timePerFrame; //hack - may have changed game speed ( otherwise ds is constant)

        //if circle point or target point...
        makeTurn(moveManager.nextMove());

        //otherwise...
        //makeTurn(0);

        avoidHills();

        sink();
        setLocalFrame();
        if (tail != null) tail.moveTo(p);
    }

    protected void sink() {
        //overrider this method for different flying machines
        v.z = 0;
    }

    private void setLocalFrame() {
	/*
	  Set i , j and k vectors so v is along
	  the y axis - ie do pitch and yaw
	*/
        Tools3d.cross(v, new Vector3d(0, 0, 1), axisX);
        Tools3d.scaleBy(axisX, 1 / Tools3d.length(axisX));

        Tools3d.clone(v, axisY);
        Tools3d.scaleBy(axisY, 1 / Tools3d.length(axisY));

        Tools3d.cross(axisX, axisY, axisZ);

        //now apply roll, if any
        if (roll == 0) return;

        Vector3d up = axisZs[roll + ROLL_STEPS];

        Vector3d axisX0 = new Vector3d();
        Vector3d axisZ0 = new Vector3d();
        Vector3d dx = new Vector3d();
        Vector3d dz = new Vector3d();

        Tools3d.clone(axisX, axisX0);
        Tools3d.clone(axisZ, axisZ0);

        Tools3d.clone(axisX0, dx);
        Tools3d.clone(axisZ0, dz);
        Tools3d.scaleBy(dx, up.x);
        Tools3d.scaleBy(dz, up.z);
        Tools3d.add(dx, dz, axisZ);

        Tools3d.clone(axisX0, dx);
        Tools3d.clone(axisZ0, dz);
        Tools3d.scaleBy(dx, up.z);
        Tools3d.scaleBy(dz, -up.x);
        Tools3d.add(dx, dz, axisX);
    }

    private void initRoll() {
	/*
	  generate an array of unit 'up' vectors for 
	  different angles of bank (v points along the
	  y axis)
	*/
        axisZs = new Vector3d[ROLL_STEPS * 2 + 1];

        for (int i = -ROLL_STEPS; i < ROLL_STEPS + 1; i++) {
            double theta = ((double) i / (double) ROLL_STEPS) * ROLL_MAX_ANGLE;
            Vector3d axisZ = new Vector3d();

            axisZ.x = (float) Math.sin(theta);
            axisZ.z = (float) Math.cos(theta);

            axisZs[i + ROLL_STEPS] = axisZ;
        }
    }

    void roll(float dir) {
        if (dir != 0) {
            //turning
            roll += dir;
            if (roll > ROLL_STEPS) roll = ROLL_STEPS;
            if (roll < -ROLL_STEPS) roll = -ROLL_STEPS;
        } else {
            //roll level
            if (roll > 1) {
                roll--;
            } else if (roll < -1) {
                roll++;
            } else {
                roll = 0;
            }
        }
    }

    public Vector3d getFocus() {
        //mid height and 'ahead'
        return new Vector3d(p.x, p.y + 1, 1);
    }

    public Vector3d getEye() {
        if (p.x > 0) {
            return new Vector3d(p.x + 2, p.y - 2, (float) 0.8);
        } else {
            return new Vector3d(p.x - 2, p.y - 2, (float) 0.8);
        }
    }

    void makeTurn(float dir) {
	/*
	  turn to the left or right.
	  for moving in a circle dv is always normal
	  to v and dv = v * v/r. take cross product 
	  with unit vertical & scale by v/r.
			
	  <dir> 
	  > 0 turn right, < 0 turn left, 
	  1 - my turn radius
	  2 - halve that etc.
	*/
        Vector3d w = new Vector3d();
        Vector3d x = new Vector3d();

        v.z = 0;    //work in xy plane
        Tools3d.cross(new Vector3d(0, 0, 1), v, w);
        Tools3d.scaleBy(w, -dir * ds / my_turn_radius);
        Tools3d.add(v, w, v);
        Tools3d.scaleToLength(v, ds); //ds is in xy only
        roll(dir);
    }

    void avoidHills() {
	/*
	  look at ground clearence both now
	  and at next proposed point
	*/

        if (moveManager.joinedCircuit()) return;
        if (app.landscape == null) return;

        Vector3d p_ = new Vector3d();
        Tools3d.add(p, v, p_);

        float h = p.z - app.landscape.getHeight(p.x, p.y);
        float h_ = p.z - app.landscape.getHeight(p_.x, p_.y);
        float dh = h_ - h;

        if (h < 0) return; //too late !

        if (dh < 0 && h < my_turn_radius) {
            //float ONE_WING = (float) 0.2;
            //float r = (h - ONE_WING) * (ds/dh) * (ds/dh);
	    /*
	      turn left or right ? see if moving right
	      a bit gives a greater h than staight on
	    */
            Vector3d w = new Vector3d();
            Tools3d.cross(v, new Vector3d(0, 0, 1), w);
            Tools3d.scaleBy(w, ds / my_turn_radius);
            Vector3d p__ = new Vector3d();
            Tools3d.add(p_, w, p__);
            float h__ = p.z - app.landscape.getHeight(p__.x, p__.y);
            if (h__ >= h_) {
                makeTurn(1); //turn right
            } else {
                makeTurn(-1); //turn left
            }
        }
    }
}

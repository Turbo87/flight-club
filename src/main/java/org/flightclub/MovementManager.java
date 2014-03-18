/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

/*
  Manage the motion of flying dots - thermalling, ridge soaring etc.
*/

public class MovementManager {
    final ModelViewer app;
    FlyingDot flyingDot = null;

    // point to fly towards
    private Vector3d targetPoint = null;
    // point to circle around
    private Vector3d circlePoint = null;
    // cloud to thermal
    Cloud cloud = null;
    // direction to fly
    private Vector3d targetDirection = null;
    // list of points to fly round
    private Circuit circuit = null;
    private Vector3d circuitPoint = null;

    private float speedOverHypot;
    int nextMoveUser = 0;
    float lastDistance = 0;
    boolean joinedCircuit = false;

    int wiggleCount = 0;
    final int wiggleSize = 5;

    static final int LEFT = -1;
    static final int STRAIGHT = 0;
    static final int RIGHT = 1;

    static final int CIRCLE_DIR = LEFT;
    static final float E_COS = (float) 0.05;
    static final float E_DIST = (float) 0.1;

    public MovementManager(ModelViewer theApp, FlyingDot theFlyingDot) {
        app = theApp;
        flyingDot = theFlyingDot;
    }

    float wiggle() {
        wiggleCount--;
        if (wiggleCount > wiggleSize * 3) {
            return -2;
        }
        if (wiggleCount > wiggleSize * 2) {
            return 2;
        }
        if (wiggleCount > wiggleSize) {
            return 2;
        }
        if (wiggleCount > 0) {
            return -2;
        }
        return 0;
    }

    /**
     * called by flyingdot each tick - turn left (-1)
     * right (+1) or straight on (0)
     */
    float nextMove() {
        if (wiggleCount > 0) {
            return wiggle();
        }

        if (nextMoveUser != 0) return nextMoveUser;

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

        // otherwise fly straight on
        return 0;
    }

    void setCircuit(Circuit inCircuit) {
        clearControllers();
        circuit = inCircuit;
        joinedCircuit = false;
        circuitPoint = circuit.next(flyingDot);
    }

    void setTargetPoint(Vector3d t) {
        clearControllers();
        targetPoint = new Vector3d(t.x, t.y, t.z);
    }

    void setCirclePoint(Vector3d c) {
        //take a copy of c (otherwise it may move eg. flyingDot.p)
        clearControllers();
        circlePoint = new Vector3d(c.x, c.y, c.z);
    }

    void setCloud(Cloud c) {
        clearControllers();
        cloud = c;
    }

    void setTargetDirection(Vector3d d) {
        clearControllers();
        targetDirection = d;
    }

    void setNextMove(int dir) {
        // user pressed key to turn
        // clear all controllers
        clearControllers();
        nextMoveUser = dir;
        app.cameraMan.cutSetup(flyingDot, flyingDot.isUser);
    }

    void clearControllers() {
        targetDirection = null;
        circlePoint = null;
        targetPoint = null;
        cloud = null;
        circuit = null;
        joinedCircuit = false;
    }

    Vector3d getCirclePoint() {
        return circlePoint;
    }

    Circuit getCircuit() {
        return circuit;
    }

    boolean joinedCircuit() {
        return circuit != null && joinedCircuit;
    }

    float headForTarget() {
        return headTowards(targetPoint.x, targetPoint.y);
    }

    private float followCircuit() {

        float x = circuitPoint.x;
        float y = circuitPoint.y;

        // hack - the circuit should do this leaning calc!
        // use fall line to calc change due to height
        x += flyingDot.p.z * circuit.fallLine.x;
        y += flyingDot.p.z * circuit.fallLine.y;

        return headTowards(x, y);
    }

    private float headTowards(float x, float y) {
        /*
         * use cross product to see if we need
         * to turn left or right. return true if
         * we are 'at' the point.
         */
        Vector3d u = new Vector3d(x - flyingDot.p.x, y - flyingDot.p.y, 0);
        Vector3d v = new Vector3d(flyingDot.v.x, flyingDot.v.y, 0);
        float d = u.length();

        if (d < flyingDot.my_turn_radius / 4) {
            //we are there
            targetPoint = null;
            if (circuit != null) reachedCircuitPoint();
            return 0;
        }

        // are we flying ~ staight towards target ?
        float dot = u.dot(v) / (flyingDot.ds * d);
        if (dot > 0.99) {
            return 0;
        }

        Vector3d c = new Vector3d();
        Tools3d.cross(v, u, c);
        float sin = c.length() / (flyingDot.ds * d);
        float sin1 = flyingDot.ds / flyingDot.my_turn_radius;

        if (sin <= sin1 * 2 && sin >= -sin1 * 2) {
            /*
             * maintain ~ current heading (with
             * a bit of fine tuning to eliminate wobble)
             * eq1: ds = r * dtheta
             * eq2: dtheta ~ sin(dtheta) ( for small dtheta )
             */

            if (c.z > 0) sin *= -1;    //left
            //System.out.println("sin: " + sin);
            float r = flyingDot.ds / sin;
            //System.out.println("Fine tune MTR/r:" + flyingDot.my_turn_radius/r);

            //convert so that 1/2 my turn radius goes to 2
            return flyingDot.my_turn_radius / r;
        }

        if (c.z > 0) {
            if (circuit == null) return -1;
            else return -2; //left
        } else {
            if (circuit == null) return 1;
            else return 2; //right
        }
    }

    void reachedCircuitPoint() {
        nextMoveUser = circuit.turnDir();    //hack
        circuitPoint = circuit.next(flyingDot);
        joinedCircuit = true;
    }

    private float circleAroundPoint() {
        return circleAround(circlePoint.x, circlePoint.y);
    }

    private float circleAround(float x, float y) {
        /*
         * use cross product of v and r
         */
        Vector3d r = new Vector3d(flyingDot.p.x - x, flyingDot.p.y - y, 0);
        float d = r.length();

        //are we close ?
        if (d > flyingDot.my_turn_radius * 3) return headTowards(x, y);

        Vector3d cross = new Vector3d();
        Tools3d.cross(r, flyingDot.v, cross);

        float dperp = cross.length() / flyingDot.ds;
        float dot = r.dot(flyingDot.v);

        if (cross.z >= 0) {

            //circling the right way
            if (dot > 0) {
                return -1;
            } else {
                if (dperp <= flyingDot.my_turn_radius) {
                    return 0; //was 0 on 26th whem it worked
                } else {
                    return -1;
                }
            }
        } else {

            //circling the wrong way
            if (d < flyingDot.my_turn_radius) {
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
            setCirclePoint(flyingDot.p);
        } else {
            circlePoint = null;
        }
    }

    void workLift() {
        /*
         * this replaces togglelift - after hugh's helpful comments
         */

        if (cloud != null || circuit != null) return;

        Hill h = null;
        if (app.landscape != null) h = app.landscape.myHill(flyingDot.p);

        if (h != null) {
            setCircuit(h.getCircuit());
            app.cameraMan.cutSetup(h, flyingDot.isUser);
            return;
        }

        Cloud c = null;
        if (app.sky != null) c = app.sky.myCloud(flyingDot.p);

        if (c != null) {
            setCloud(c);
            app.cameraMan.cutSetup(cloud, flyingDot.isUser);
            return;
        }

        // no cloud or ridge - do a wiggle
        wiggleCount = wiggleSize * 4 + 1;
    }

    void toggleLift() {
        //System.out.println("Toggle lift");
        if (cloud != null) {
            //System.out.println("set cloud to null");
            cloud = null;
            app.cameraMan.cutSetup(flyingDot, flyingDot.isUser);
            return;
        }

        if (circuit != null) {
            //System.out.println("set circuit to null");
            circuit = null;
            circuitPoint = null;
            app.cameraMan.cutSetup(flyingDot, flyingDot.isUser);
            return;
        }

        if (circlePoint != null) {
            //System.out.println("set circle point to null");
            circlePoint = null;
            app.cameraMan.cutSetup(flyingDot, flyingDot.isUser);
            return;
        }

        Cloud c = app.sky.myCloud(flyingDot.p);
        if (c != null) {
            //System.out.println("set cloud !");
            setCloud(c);
            app.cameraMan.cutSetup(cloud, flyingDot.isUser);
            return;
        }

        Hill h = app.landscape.myHill(flyingDot.p);
        if (h != null) {
            //System.out.println("set hill !");
            setCircuit(h.getCircuit());
            app.cameraMan.cutSetup(h, flyingDot.isUser);
            return;
        }

        //no cloud or ridge - do a wiggle
        //System.out.println("no cloud or ridge, so circle !");
        wiggleCount = wiggleSize * 4 + 1;
    }

    float thermal() {
        return circleAround(cloud.getX(flyingDot.p.z), cloud.getY(flyingDot.p.z));
    }
}

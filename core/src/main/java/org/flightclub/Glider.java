/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;

/**
 * a glider that sniffs out lift
 */
public class Glider extends FlyingBody {
    boolean landed = true;
    int tryLater = 0;

    // hack - only glider user should know about this
    boolean demoMode = true;

    boolean reachedGoal;

    // where on the polar are we
    int polarIndex = 0;

    // 4 vars for hack to delay cuts
    boolean cutPending = false;
    int cutWhen;
    CameraSubject cutSubject = null;
    int cutCount = 0;

    // hack var for camera position - left or right depending
    int lastEyeX = 1;
    boolean triggerLoading = false;

    // units of dist (km) per time (minute)
    final static float SPEED = (float) 1;
    // i.e. glide angle of 8
    final static float SINK_RATE = (float) (-1.0 / 8);
    final static float TURN_RADIUS = (float) 0.3;
    // polar curve
    final static float[][] POLAR = {{1, 1}, {(float) 1.5, (float) 2.1}};

    public static final int TAIL_LENGTH = 40;
    public static final Color TAIL_COLOR = Color.LIGHT_GRAY;

    /**
     * default constructor - this glider is not being piloted by the user
     */
    public Glider(XCGame app, Vector3d p) {
        this(app, p, false);
    }

    public Glider(XCGame app, Vector3d p, boolean isUser) {
        super(app, SPEED, TURN_RADIUS, isUser);

        GliderShape gliderShape;
        if (!isUser) {
            gliderShape = new GliderShape(app);
        } else {
            gliderShape = new GliderShape(app, Color.YELLOW);
        }

        this.init(gliderShape, p);
        bodyHeight = GliderShape.HEIGHT;
        gotoNextLiftSource();
    }

    public Glider(XCGame app, Vector3d p, boolean isUser, boolean isRigid) {
        //hack to get a couple of faster pink machines
        //sailplane/rigid
        super(app, SPEED * (float) 1.5, TURN_RADIUS * (float) 1.2, isUser);

        GliderShape gliderShape = new GliderShape(app, Color.PINK);

        this.init(gliderShape, p);
        bodyHeight = GliderShape.HEIGHT;
        gotoNextLiftSource();
    }

    /**
     * fly downwind to next lift source
     * goto hill if there is one, otherwise goto a cloud
     * NB inform the cameraman of this event ??
	 */
    void gotoNextLiftSource() {
        Hill hill = null;
        Cloud cloud = null;

        if (app.landscape != null) {
            hill = app.landscape.nextHill(new Vector3d(p.x, p.y + 2, p.z));
        }

        if (app.sky != null) {
            cloud = app.sky.nextCloud(new Vector3d(p.x, p.y + 2, p.z));
        }

        if (!demoMode) return;
        cutPending = false;

        if (hill != null) {
            this.moveManager.setCircuit(hill.getCircuit());

            cutPending = true;
            cutWhen = whenArrive(hill.x0, hill.y0) - app.cameraMan.CUT_LEN * 2; //??2
            cutSubject = hill;
            cutCount = 0;
            //System.out.println("Cut when: " + cutWhen);
            return;
        }

        //no hill - look at clouds
        if (cloud == null) {
            //backtrack upwind

            if (app.sky != null) cloud = app.sky.prevCloud(new Vector3d(p.x, p.y - 2, p.z));

            if (cloud == null) {
                //try again later, fly downwind for now
                this.moveManager.setTargetPoint(new Vector3d(p.x, p.y + 8, p.z));
                tryLater = 25;
                return;
            }
        }

        //glide to cloud
        this.moveManager.setCloud(cloud);

        cutPending = true;
        cutWhen = whenArrive(cloud.p.x, cloud.p.y) - app.cameraMan.CUT_LEN * 2;
        cutSubject = cloud;
        cutCount = 0;
        //System.out.println("Cut when: " + cutWhen);


        //app.cameraMan.cutSetup(cloud, isUser);
    }

    @Override
    protected void createTail() {
        tail = new Tail(app, TAIL_LENGTH, TAIL_COLOR);
        tail.init(p);
    }

    /**
     * take glider's sink rate and add any
     * ridge or thermal lift
     */
    @Override
    protected void sink() {
        //float lift = SINK_RATE;
        float lift = POLAR[polarIndex][1] * SINK_RATE;
		
        /*
          if (true) {
          v.z=  0;
          return;
          }
        */

        if (app.landscape != null) {
            Hill hill = app.landscape.getHillAt(p);
            if (hill != null) {
                if (p.z < hill.maxH + (float) 0.1) {
                    //lift += (float) 1.5 * - SINK_RATE;
                    lift += hill.getLift(p);
                } else {
                    //climbed to top of ridge
                    //lift = 0;
                    lift += hill.getLift(p);
                    if (demoMode && moveManager.joinedCircuit()) {
                        moveManager.clearControllers();
                        gotoNextLiftSource();
                    }
                }
            }
        }

        if (app.sky != null) {
            Cloud cloud = app.sky.getCloudAt(p);
            if (cloud != null) {
                if (p.z < Sky.getCloudBase() - this.getBodyHeight()) {
                    lift += cloud.getLift(p);
                } else {
                    //stick to base of cloud and f*** off downwind
                    lift = 0;
                    if (demoMode) {
                        moveManager.clearControllers();
                        gotoNextLiftSource();
                    }
                }
            }
        }

        v.z = lift * app.timePerFrame;

        //check not going underground
        if (p.z <= 0 && v.z < 0) {
            v.z = 0;
            if (!landed) landed();
        }

        if (tryLater > 0) {
            tryLater--;
            if (tryLater == 0) gotoNextLiftSource();
        }

        if (moveManager.cloud != null && moveManager.cloud.decaying) {
            //System.out.println("Decaying cloud - move on");
            moveManager.clearControllers();
            if (isUser) {
                app.cameraMan.cutSetup(this, isUser);
            } else {
                gotoNextLiftSource();
            }
        }
    }

    void landed() {
        roll = 0;
        tail.reset(p);
        landed = true;
        //app.cameraMan.cutSetup(this, isUser);
    }

    @Override
    public void tick(float delta) {

        if (isUser && demoMode) return;

        if (isUser && !landed)
            app.textMessage = "D: " + (int) (p.y / 2) + "km  T: " + (int) app.time / 2 + "mins  H: " + (int) ((p.z / 2) * 1500) + "m ";

        //landed ?
        if (landed && isUser) {
            app.textMessage = "You have landed - you flew " + (int) (p.y / 2) + "km. Press <y> to fly again.";
        }
        if (landed) return;

        //reached goal ?
        if (reachedGoal && isUser) {
            app.textMessage = "Well done ! You have reached goal. You flew " + (int) (p.y / 2) + "km in " + (int) app.time / 2 + " mins. Press <y> to fly again.";
        }
        if (reachedGoal) return;

        super.tick(delta);

        //delayed cut hack 5/10
        if (cutPending) {
            cutCount++;
            if (cutCount >= cutWhen) cutNow();
        }

        if (app.landscape != null && triggerLoading) app.landscape.loadTilesAround(p);
        if (!reachedGoal) reachedGoal = app.landscape.reachedGoal(p);
    }

    /**
     * return number of ticks till we get there
     * nb. this assumes stationary target and no wind
     */
    int whenArrive(float x, float y) {
        float d = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
        d = (float) Math.sqrt(d);
        return (int) (d / ds);
    }

    private void cutNow() {
        /*
          this call used to be in gotoliftsource
          moved for timing - wait until glider is
          close to the hill/thermal so the camera
          does not get ahead of it

          todo - camera decide how to track without
          losing site of the glider
        */
        app.cameraMan.cutSetup(cutSubject, isUser);
        cutPending = false;
        cutCount = 0;
        cutSubject = null;
    }

    @Override
    public Vector3d getFocus() {
        float z;
        if (p.z < 0.5) z = (float) 0.5;
        else z = (float) 0.5 + (p.z - (float) 0.5) / 2;
        return new Vector3d(p.x, p.y + 1, z);
    }

    @Override
    public Vector3d getEye() {
        return new Vector3d(p.x + (float) 0.2, p.y - 2, p.z + (float) 0.3); //(float)0.8
    }

    void takeOff(Vector3d inP) {
        /*
          hack ? should be in glider user ?
        */

        //if (!landed) return;

        setPolarIndex(0);
        v = new Vector3d(0, ds, 0);
        p = new Vector3d(inP.x, inP.y, inP.z);

        landed = false;
        reachedGoal = false;
        tail.reset(p);

        demoMode = true;
        gotoNextLiftSource();

        if (isUser) {
            app.cameraMan.setMode(CameraMan.Mode.SELF);
            demoMode = false;
        }
    }

    /*
     * choose a point on the polar
     * determines speed and sink rate
     *
     * bit of a mess as this class knows about sink rate
     * and super class manages horizontal speed
     */
    void setPolarIndex(int i) {
        polarIndex = i;
        super.setSpeed(POLAR[polarIndex][0] * SPEED);
    }
}


/*
  Cloud.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Dan Burton , Nov 2001 
*/

import java.awt.*;
import java.util.Vector;

class Cloud implements CameraSubject, ClockObserver {
    final ModelViewer app;
    final Object3dWithShadow object3d;
    Vector3d p = new Vector3d();
    float radius;
    final float maxRadius;
    final boolean solid = true;
    final Color color;
    final Vector3d[] corners = new Vector3d[8];
    final boolean inForeGround;
    ThermalTrigger trigger = null;

    boolean decaying = false;
    float age = 0;
    final int t_nose;
    int t_mature;
    final int t_tail;

    final double[] theta = new double[4];
    final double[] landa = new double[4];

    final float liftMax;
    final float myRnd;    //see getEye

    final float windSlope = (float) 0.5;//0.1 //lean towards +y due to wind (1 equals 45 degrees)
    float ds;

    static final float LIFT_FN_OUTER = 1;
    static final float LIFT_FN_INNER = (float) 0.5;
    final static double pi = 3.14159265;
    static final int CLOUD_COLOR = 230;
    static final int CLOUD_COLOR_STEP = 20;//how much darker are strong clouds

    public Cloud(ModelViewer inApp, float x, float y, int inDuration, int inStrength) {
        app = inApp;
        object3d = new Object3dWithShadow(app);

        for (int i = 0; i < 8; i++)
            corners[i] = new Vector3d();

        //units of time - model minutes
        t_nose = 10;
        t_mature = inDuration;
        t_tail = 8;

        age = (float) 0.1;//small but non zero
        //ds = Sky.getWind()/app.getFrameRate();
        myRnd = (float) (Tools3d.rnd(0, 1));//for camera angle
        p = new Vector3d(x, y, Sky.getCloudBase());

	/*
      cloud strength measured in multiples of glider (min) sink rate
	  e.g. 1 > climb at sink rate, 2 > climb at twice sink rate etc
	  also, stronger clouds are bigger and darker
	*/
        liftMax = -(1 + inStrength) * Glider.SINK_RATE;
        int c = CLOUD_COLOR - (inStrength - 1) * CLOUD_COLOR_STEP;
        if (inStrength == 1)
            color = new Color(c, c, c);
        else
            //darker, but keep some blue
            color = new Color(c, c, c);

        maxRadius = inStrength;  //(float) Math.sqrt(inStrength); was 1

        setSphericals();
        setCorners();
        buildSurfaces();
        app.clock.addObserver(this);

        //add to lift profile ?
        inForeGround = (p.x < Landscape.TILE_WIDTH / 2 && p.x > -Landscape.TILE_WIDTH / 2);
        if (inForeGround) app.sky.addCloud(this);

    }

    public Cloud(ModelViewer inApp, float x, float y, int inDuration) {
        //no cloud strength specifed, so use default of 1 = give climbs equal to min sink
        this(inApp, x, y, inDuration, 1);
    }

    void destroyMe() {
        object3d.destroyMe();
        if (inForeGround) app.sky.removeCloud(this);
        app.clock.removeObserver(this);
        if (trigger != null) trigger.clouds.removeElement(this);
    }

    private void buildSurfaces() {
        Vector<Vector3d> wire;

        //front
        wire = new Vector<>();
        wire.addElement(corners[1]);
        wire.addElement(corners[0]);
        wire.addElement(corners[3]);
        wire.addElement(corners[2]);
        wire.addElement(corners[1]);
        object3d.addWire(wire, color, solid);

        //back
        wire = new Vector<>();
        wire.addElement(corners[4]);
        wire.addElement(corners[5]);
        wire.addElement(corners[6]);
        wire.addElement(corners[7]);
        wire.addElement(corners[4]);
        object3d.addWire(wire, color, solid);

        //bot
        wire = new Vector<>();
        wire.addElement(corners[0]);
        wire.addElement(corners[4]);
        wire.addElement(corners[7]);
        wire.addElement(corners[3]);
        wire.addElement(corners[0]);
        object3d.addWireWithShadow(wire, color, solid, true);

        //top - use a convex tile
        Vector3d[] topCorners = new Vector3d[4];

        topCorners[0] = corners[1];
        topCorners[1] = corners[2];
        topCorners[2] = corners[6];
        topCorners[3] = corners[5];

        object3d.addTile(topCorners, color, true, false);

        //right
        wire = new Vector<>();
        wire.addElement(corners[0]);
        wire.addElement(corners[1]);
        wire.addElement(corners[5]);
        wire.addElement(corners[4]);
        wire.addElement(corners[0]);
        object3d.addWire(wire, color, solid);

        //left
        wire = new Vector<>();
        wire.addElement(corners[2]);
        wire.addElement(corners[3]);
        wire.addElement(corners[7]);
        wire.addElement(corners[6]);
        wire.addElement(corners[2]);
        object3d.addWire(wire, color, solid);

    }

    public boolean isUnder(Vector3d inP) {
	/*
	  only compute lift if within bounding box
	*/
        //if (age > t_nose + t_mature) return false;

        if (inP.x > p.x + LIFT_FN_OUTER) return false;
        if (inP.x < p.x - LIFT_FN_OUTER) return false;
        if (inP.y > getY(inP.z) + LIFT_FN_OUTER) return false;
        if (inP.y < getY(inP.z) - LIFT_FN_OUTER) return false;

        return (getLift(inP) > 0);
    }

    public void tick(Clock c) {
        age += app.timePerFrame;
        if (age > t_mature + t_nose + t_tail * 0.5) decaying = true;

        if (age > t_mature + t_nose + t_tail) {
            destroyMe();
            return;
        }

        p.y += Sky.getWind() * app.timePerFrame;
        p.z = Sky.getCloudBase();
        setCorners();
        object3d.updateShadow();
    }

    float getRadius() {
	/*
	  cloud radius is a function of it's age.
	  here's the model...
			
	  volume of rising air, dv ~ constant.
	  decay ~ surface area.
	  dynamic equilibrium, dv = decay, at maturity.
	  dv = 0 at old age.
	*/
        float fn;
        if (age <= t_nose) {
            fn = (float) Math.sqrt((double) age / t_nose);
        } else if (age > t_nose && age <= t_nose + t_mature) {
            fn = 1;
        } else if (age > t_mature + t_nose && age <= t_mature + t_nose + t_tail) {
            fn = (float) Math.sqrt(1 - (double) (age - t_mature - t_nose) / t_tail);
        } else {
            fn = 0;
        }
        return fn * maxRadius;
    }

    float getRadiusBase(float radius) {
	/*
	  make base decay quicker than top of cloud
	*/
        if (age > t_mature + t_nose) {
            float fn = (float) Math.sqrt(1 - (double) (age - t_mature - t_nose) / t_tail);
            fn = 2 * (fn - (float) 0.5);
            if (fn < 0.2) fn = (float) 0.2;//0
            return radius * fn;
        } else {
            return radius;
        }
    }

    void setCorners() {
        Vector3d v = new Vector3d();
        float radius = getRadius();
        float radiusBase = getRadiusBase(radius);

        //front face
        sphToXYZ(radiusBase, theta[0], 0.0, v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[0]);

        sphToXYZ(radiusBase, theta[3], 0, v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[3]);

        sphToXYZ(radius, theta[3], landa[3], v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[2]);

        sphToXYZ(radius, theta[0], landa[0], v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[1]);

        //back face
        sphToXYZ(radiusBase, theta[1], 0.0, v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[4]);

        sphToXYZ(radius, theta[1], landa[1], v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[5]);

        sphToXYZ(radius, theta[2], landa[2], v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[6]);

        sphToXYZ(radiusBase, theta[2], 0.0, v);
        Tools3d.add(v, p, v);
        Tools3d.clone(v, corners[7]);
    }

    void setSphericals() {
        double lower, upper;

        //thetas
        for (int quad = 0; quad < 4; quad++) {
            lower = quad * 90.0;
            upper = lower + 80.0;
            theta[quad] = Tools3d.rnd(lower, upper);
        }

        //landas
        for (int quad = 0; quad < 4; quad++) {
            landa[quad] = Tools3d.rnd(20.0, 50.0);//70
        }
    }

    void sphToXYZ(float r, double a, double b, Vector3d v) {
        //convert degrees to radians, a - theta, b -landa
        a *= (pi / 180);
        b *= (pi / 180);

        v.x = (float) (r * Math.cos(b) * Math.cos(a));
        v.y = (float) (r * Math.cos(b) * Math.sin(a));
        v.z = (float) (r * Math.sin(b));

        //wind slope - wind blows cloud tops downwind
        v.y += windSlope * v.z;
    }

    public Vector3d getFocus() {
        return new Vector3d(p.x, p.y + 2, (float) 1);
    }

    public Vector3d getEye() {
        int dx;
        if (p.x > 0) dx = 1;
        else dx = -1;
        if (myRnd > 0.7) {
            return new Vector3d(p.x + 3 * dx, p.y - 3, (float) 0.1);
        } else if (myRnd > 0.3) {
            return new Vector3d(p.x + 1 * dx, p.y - 5, (float) 1.5);
        } else {
            return new Vector3d(p.x, p.y - (float) 2.5, (float) 1.2);
        }
    }

    float getX(float z) {
        //no cross wind
        return p.x;
    }

    float getY(float z) {
        float d = Sky.getCloudBase() - z;
        return p.y - d * windSlope;
    }

    float getLift(Vector3d inP) {
	/*
	  lift is a function of r (dist from thermal center)
	*/
        float dx = p.x - inP.x;
        float dy = getY(inP.z) - inP.y;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float lift;

        if (inP.z > Sky.getCloudBase()) return 0;
        //if (age > t_nose + t_mature) return 1;

        if (r >= LIFT_FN_OUTER) {
            lift = 0;
        } else if (r > LIFT_FN_INNER) {
            lift = liftMax * (1 - (r - LIFT_FN_INNER) / (LIFT_FN_OUTER - LIFT_FN_INNER));
        } else {
            //we are in the core
            lift = liftMax;
        }

        return lift;
    }

}


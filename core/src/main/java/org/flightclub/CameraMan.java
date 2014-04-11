/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.graphics.Color;
import org.flightclub.graphics.ColorFactory;

/*
  todo - seperate into two classes
  - generic 3d framework camera functionality
  - XCGame extension of above class
*/

public class CameraMan {
    final XCGame app;
    public final Vector3d lightRay;
    public float zoom = 1;

    private float distance = 0;
    private float[][] matrix;

    private final int screenWidth;
    private final int screenHeight;
    private final float theScale;

    private Vector3d eye;
    private Vector3d focus;

    private static final int BACKGROUND_R = 255;
    private static final int BACKGROUND_G = 255;
    private static final int BACKGROUND_B = 255;
    private static final Color BACKGROUND = ColorFactory.create(
            BACKGROUND_R, BACKGROUND_G, BACKGROUND_B);

    private static final float DEPTH_OF_VISION = Landscape.TILE_WIDTH * (float) 2.5;
    private static final float AMBIENT_LIGHT = (float) 0.3;

    public static final float CAMERA_MOVEMENT_DELTA = (float) 0.1;

    CameraSubject cameraSubject; //populate using cutSetup
    CameraSubject subject1;
    CameraSubject subject2;

    public enum Mode {
        SELF,
        GAGGLE,
        PLAN,
        TILE,
    }

    Mode mode = Mode.SELF;

    int cutCount = 0;
    int cut2Count = 0;

    /** number of steps we accelerate over */
    static final int CUT_RAMP = 12;

    /** steps to glide between POVs */
    static final int CUT_LEN = 75;

    Vector3d deye, dfocus;
    Vector3d eyeGoto, focusGoto;

    static final int PLAN_H = 20;
    static final int PLAN_Y_OFFSET = 4;

    CameraMan(XCGame theApp) {
        app = theApp;

        //get the canvas size
        screenWidth = app.envInterface.getWidth();
        screenHeight = app.envInterface.getHeight();
        theScale = screenHeight * (float) 1.1; //defines lens angle - smaller num -> wider angle

        //starting position and light
        eye = new Vector3d(3, 0, 0);
        focus = new Vector3d(0, 0, 0);

        lightRay = new Vector3d(1, 1, -3);
        //lightRay = new Vector3d(-2,2,-1);
        lightRay.makeUnit();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    void setMode(Mode mode) {
        this.mode = mode;

        if (mode == Mode.SELF && subject1 != null) {
            cutCount = 0;
            cutSetup(subject1, true);
            return;
        }

        if (mode == Mode.GAGGLE && subject2 != null) {
            cutCount = 0;
            cut2Count = 0;
            cutSetup(subject2, false);
            return;
        }

        if (mode == Mode.PLAN) {

            //hack - should extend generic cameraman
            boolean user = (app.mode == XCGame.Mode.USER);

            if (subject1 != null && user) {
                focus = subject1.getFocus();
                focus.x = 0;
                focus.y += PLAN_Y_OFFSET;
                eye = new Vector3d(Landscape.TILE_WIDTH / 2, focus.y, PLAN_H);
                cameraSubject = subject1;
            } else if (subject2 != null) {
                focus = subject2.getFocus();
                focus.x = 0;
                focus.y += PLAN_Y_OFFSET;
                eye = new Vector3d(Landscape.TILE_WIDTH / 2, focus.y, PLAN_H);
                cameraSubject = subject2;
            } else {
                focus = new Vector3d(0, Landscape.TILE_WIDTH, 0);
                eye = new Vector3d(10, Landscape.TILE_WIDTH, PLAN_H);
                cameraSubject = null;
            }
            cutCount = 0;
        }


        if (mode == Mode.TILE && app.landscape != null) {
            cutCount = 0;
            cut2Count = 0;
            cutSetup(app.landscape, true);
        }
    }

    /**
     * how much light falls on a surface with this normal - take dot product
	 */
    float surfaceLight(Vector3d inNormal) {
        float dot = lightRay.dot(inNormal);
        dot = (-dot + 1) / 2;

        //fri 1 mar 2002 - some under lighting for clouds
        if (inNormal.z < -0.99) dot += 0.3;

        return dot * (1 - AMBIENT_LIGHT) + AMBIENT_LIGHT;
    }

    /**
     * update camera position
     */
    public void tick() {
        if (cameraSubject == null) return;
        if (cut2Count > 0) cut2Count--; //for watch mode 2

        if (cutCount > 0) {
            cutStep();
        }

        followSubject();
    }

    void followSubject() {
        //add in movement of our subject
        if (mode != Mode.PLAN) {
            Vector3d eNew = cameraSubject.getEye();
            Vector3d fNew = cameraSubject.getFocus();

            Vector3d de = eNew.minus(eyeGoto);
            Vector3d df = fNew.minus(focusGoto);

            eye.add(de);
            focus.add(df);

            //note subjects new position for next iteration
            eyeGoto = eNew;
            focusGoto = fNew;
        } else {
            //track whilst maintaining a constant camera angle
            Vector3d f = cameraSubject.getFocus();
            f.x = 0;
            f.y += PLAN_Y_OFFSET;
            moveFocus(f);
        }
    }

    /**
     * glide eye and focus to new positions using N steps. here we set it up and
     * it'll unwind over the next N ticks
     *
     * @param isUser true: call from user glider, false: one of the gagggle
     */
    void cutSetup(CameraSubject subject, boolean isUser) {
        if (mode == Mode.PLAN) return;

        if (cutCount > 0 && mode == Mode.GAGGLE) {
            //ignore this call if already doing a cut
            return;
        }

        if (!isUser && mode != Mode.GAGGLE) {
            //filter out all calls from sniffers unless watching gaggle
            return;
        }

        //gaggle mode too jumpy
        if (mode == Mode.GAGGLE && cut2Count > 0) return;

        cameraSubject = subject;
        cutCount = CUT_LEN;
        cut2Count = cutCount * 2;

        eyeGoto = cameraSubject.getEye();
        focusGoto = cameraSubject.getFocus();

        deye = eyeGoto.minus(eye);
        dfocus = focusGoto.minus(focus);

        /*
         * eye accelerates from 0 upto velocity
         * deye over cutRamp steps, tracks at deye then
         * slows to zero over cutRamp steps. similarly for focus.
         */
        deye.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
        dfocus.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
    }

    /**
     * iterate the cut. nb. the point we are cutting to may be on the move
     */
    @SuppressWarnings("StatementWithEmptyBody")
    void cutStep() {
        Vector3d deye_ = new Vector3d(deye);
        Vector3d dfocus_ = new Vector3d(dfocus);
        float s;

        if (cutCount > CUT_LEN - CUT_RAMP) {
            //accelerating
            s = (float) (CUT_LEN - cutCount) / CUT_RAMP;
            //System.out.println("Cut acc, s: " + s);
            deye_.scaleBy(s);
            dfocus_.scaleBy(s);
        } else if (cutCount < CUT_RAMP) {
            //decelerating
            s = (float) cutCount / CUT_RAMP;
            //System.out.println("Cut dec, s: " + s);
            deye_.scaleBy(s);
            dfocus_.scaleBy(s);
        } else {
            //const speed - no need to scale
        }

        eye.add(deye_);
        focus.add(dfocus_);

        cutCount--;
    }


    public Vector3d getEye() {
        return eye;
    }

    public void setEye(float x, float y, float z) {
        eye.set(x, y, z);
    }

    public Vector3d getFocus() {
        return focus;
    }

    public void setFocus(float x, float y, float z) {
        focus.set(x, y, z);
    }

    public float getDistance() {
        return distance;
    }

    public float[][] getMatrix() {
        return matrix;
    }

    /**
     * rotate eye about z axis by xy radians and up/down by z
     */
    void rotateEyeAboutFocus(float dtheta, int dz) {
        //get ray focus -> eye
        //Vector3d ray = new Vector3d();
        //Tools3d.subtract(eye,focus,ray);

        Vector3d ray = eye.minus(focus);

        //transform ray
        float[][] m = Tools3d.rotateX(new Vector3d(1, dtheta, 0));
        Tools3d.applyTo(m, ray, ray);

        if (dz > 20) ray.z += distance / (XCGame.FRAME_RATE * 4);
        if (dz < -20) ray.z -= distance / (XCGame.FRAME_RATE * 4);

        //reposition eye
        eye.set(ray).add(focus);
        if (eye.z < 0) eye.z = 0;
    }

    /**
     * move focus, maintaining angle of view
     */
    void moveFocus(Vector3d f) {
        Vector3d ray = eye.minus(focus);
        focus.set(f);
        eye.set(ray).add(focus);
    }

    /**
     * rotation such that eye is looking down +x axis at origin
     */
    void setMatrix() {
        Vector3d ray = eye.minus(focus);
        matrix = Tools3d.rotateX(ray);
        distance = ray.length();
    }

    /**
     * scale the y and z co-ords so a 1 by 1 square
     * fills the screen when viewed from a distance of ??
     *
     * origin appears center screen.
     * nb flip z as screen coords have origin at top left !
     *
     * 1/10 try double scale (ie. half camera angle)
     */
    public void scaleToScreen(Vector3d v_) {
        v_.y *= theScale;    //preserve aspect ratio ? screenWidth;
        v_.y += screenWidth / 2;

        v_.z *= -theScale;
        v_.z += screenHeight / 2;
    }

    /**
     * mute distant colors.
     *
     * x is ~ distance of surface from camera
     * since we are using the transformed coords
     */
    Color foggyColor(float x, Color c) {
        if (x >= 0)
            return c;

        x *= -1;

        if (x > DEPTH_OF_VISION)
            return BACKGROUND;

        int r, g, b, r_, g_, b_;

        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();

        float f = x / DEPTH_OF_VISION;
        r_ = (int) (r + f * (BACKGROUND_R - r));
        g_ = (int) (g + f * (BACKGROUND_G - g));
        b_ = (int) (b + f * (BACKGROUND_B - b));

        return ColorFactory.create(r_, g_, b_);
    }

    public void move(float dx, float dy) {
        eye.x += dx;
        eye.y += dy;
        focus.x += dx;
        focus.y += dy;
    }
}



/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;

import java.awt.event.KeyEvent;

/*
  todo - seperate into two classes
  - generic 3d framework camera functionality
  - XCGame extension of above class
*/

public class CameraMan implements EventInterface {
    final ModelViewer app;
    public final Vector3d lightRay;
    public float zoom = 1;

    private float distance = 0;
    private float[][] matrix;

    private final int screenWidth;
    private final int screenHeight;
    private final float theScale;

    private Vector3d eye;
    private Vector3d focus;

    private final int rBackground = 255;
    private final int gBackground = 255;
    private final int bBackground = 255;

    private final float depthOfVision = Landscape.TILE_WIDTH * (float) 2.5; // //64; good for jet trails
    static final float AMBIENT_LIGHT = (float) 0.3;

    CameraSubject cameraSubject; //populate using cutSetup
    CameraSubject subject1;
    CameraSubject subject2;

    static final int WATCH_1 = 0;
    static final int WATCH_2 = 1;
    static final int WATCH_PLAN = 2;
    static final int WATCH_TILE = 3;
    int mode = WATCH_1;

    int cutCount = 0;
    int cut2Count = 0;
    static final int CUT_RAMP = 12; //number of steps we accelerate over
    static final int CUT_LEN = 75; //steps to glide between POVs
    Vector3d deye, dfocus;
    Vector3d eyeGoto, focusGoto;

    static final int PLAN_H = 20;
    static final int PLAN_Y_OFFSET = 4;

    CameraMan(ModelViewer theApp) {
        app = theApp;
        app.eventManager.addNotification(this);

        //get the canvas size
        screenWidth = app.modelCanvas.getSize().width;
        screenHeight = app.modelCanvas.getSize().height;
        theScale = screenHeight * (float) 1.1; //defines lens angle - smaller num -> wider angle

        //starting position and light
        eye = new Vector3d(3, 0, 0);
        focus = new Vector3d(0, 0, 0);

        lightRay = new Vector3d(1, 1, -3);
        //lightRay = new Vector3d(-2,2,-1);
        lightRay.makeUnit();
    }

    void setMode(int inMode) {

        if (inMode == WATCH_1 && subject1 != null) {
            mode = inMode;
            cutCount = 0;
            cutSetup(subject1, true);
            return;
        }

        if (inMode == WATCH_2 && subject2 != null) {
            mode = inMode;
            cutCount = 0;
            cut2Count = 0;
            cutSetup(subject2, false);
            return;
        }

        if (inMode == WATCH_PLAN) {

            //hack - should extend generic cameraman
            boolean user = ((XCGame) app).mode == XCGame.Mode.USER;

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
            mode = inMode;
            cutCount = 0;
        }


        if (inMode == WATCH_TILE && app.landscape != null) {
            mode = inMode;
            cutCount = 0;
            cut2Count = 0;
            cutSetup(app.landscape, true);
        }
    }

    float surfaceLight(Vector3d inNormal) {
    /*
	  how much light falls on a surface
	  with this normal - take dot product
	*/

        float dot = lightRay.dot(inNormal);
        dot = (-dot + 1) / 2;

        //fri 1 mar 2002 - some under lighting for clouds
        if (inNormal.z < -0.99) dot += 0.3;

        return dot * (1 - AMBIENT_LIGHT) + AMBIENT_LIGHT;


    }

    public void tick() {
	/*
	  update camera position
	*/
        if (cameraSubject == null) return;
        if (cut2Count > 0) cut2Count--; //for watch mode 2

        if (cutCount > 0) {
            cutStep();
        }

        followSubject();
    }

    void followSubject() {
        //add in movement of our subject
        if (mode != WATCH_PLAN) {
            Vector3d eNew = cameraSubject.getEye();
            Vector3d fNew = cameraSubject.getFocus();

            Vector3d de = eNew.subtracted(eyeGoto);
            Vector3d df = fNew.subtracted(focusGoto);

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

    void cutSetup(CameraSubject subject, boolean isUser) {
	/*
	  glide eye and focus to new positions
	  using N steps. here we set it up and
	  it'll unwind over the next N ticks

	  <isUser> 	t: call from user glider
	  f: one of the gagggle
	*/
        if (mode == WATCH_PLAN) return;

        if (cutCount > 0 && mode == WATCH_2) {
            //ignore this call if already doing a cut
            return;
        }

        if (!isUser && mode != WATCH_2) {
            //filter out all calls from sniffers unless watching gaggle
            return;
        }

        //gaggle mode too jumpy
        if (mode == WATCH_2 && cut2Count > 0) return;

        cameraSubject = subject;
        cutCount = CUT_LEN;
        cut2Count = cutCount * 2;

        eyeGoto = cameraSubject.getEye();
        focusGoto = cameraSubject.getFocus();

        deye = eyeGoto.subtracted(eye);
        dfocus = focusGoto.subtracted(focus);

	/*
	  eye accelerates from 0 upto velocity
	  deye over cutRamp steps, tracks at deye then
	  slows to zero over cutRamp steps. similarly 
	  for focus.
	*/
        deye.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
        dfocus.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
    }

    void cutStep() {
	/*
	  iterate the cut. nb. the point we
	  are cutting to may be on the move
	*/
        Vector3d deye_ = new Vector3d(deye.x, deye.y, deye.z);
        Vector3d dfocus_ = new Vector3d(dfocus.x, dfocus.y, dfocus.z);
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

    public void setEye(float x, float y, float z) {
        eye.set(x, y, z);
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

    Vector3d getFocus() {
        return new Vector3d(focus.x, focus.y, focus.z);
    }

    Vector3d getEye() {
        return new Vector3d(eye.x, eye.y, eye.z);
    }

    //rotate eye about z axis by xy radians and up/down by z
    void rotateEyeAboutFocus(float dtheta, int dz) {
        //get ray focus -> eye
        //Vector3d ray = new Vector3d();
        //Tools3d.subtract(eye,focus,ray);

        Vector3d ray = eye.subtracted(focus);

        //transform ray
        float[][] m = Tools3d.rotateX(new Vector3d(1, dtheta, 0));
        Tools3d.applyTo(m, ray, ray);

        if (dz > 20) ray.z += distance / (app.getFrameRate() * 4);
        if (dz < -20) ray.z -= distance / (app.getFrameRate() * 4);

        //reposition eye
        eye.set(ray).add(focus);
        if (eye.z < 0) eye.z = 0;
    }

    void moveFocus(Vector3d f) {
	/*
	  move focus, maintaining angle of view
	*/
        Vector3d ray = eye.subtracted(focus);
        focus.set(f);
        eye.set(ray).add(focus);
    }

    void setMatrix() {
	/*
	  rotation such that eye is looking
	  down +x axis at origin
	*/
        Vector3d ray = eye.subtracted(focus);
        matrix = Tools3d.rotateX(ray);
        distance = ray.length();
    }

    public void scaleToScreen(Vector3d v_) {
	/*
	  scale the y and z co-ords so a 1 by 1 square
	  fills the screen when viewed from a distance
	  of ??
			
	  origin appears center screen. 
	  nb flip z as screen coords have origin at top left !
			
	  1/10 try double scale (ie. half camera angle)
	*/
        v_.y *= theScale;    //preserve aspect ratio ? screenWidth;
        v_.y += screenWidth / 2;

        v_.z *= -theScale;
        v_.z += screenHeight / 2;
    }

    Color foggyColor(float x, Color c) {
	/*
	  mute distant colors
	  x is ~ distance of surface from camera
	  since we are using the transformed coords
	*/

        if (x >= 0) return c;
        x *= -1;

        int r, g, b, r_, g_, b_;

        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();

        if (x > depthOfVision) {
            r_ = rBackground;
            g_ = gBackground;
            b_ = bBackground;
        } else {
            float f = x / depthOfVision;
            r_ = (int) (r + f * (rBackground - r));
            g_ = (int) (g + f * (gBackground - g));
            b_ = (int) (b + f * (bBackground - b));
        }

        return new Color(r_, g_, b_);
    }

    void toggleMode() {
	/*
	  toggle between watching glider user
	  and watching the gaggle
	*/
        if (mode == WATCH_1) {
            mode = WATCH_2;
            if (subject2 != null) cutSetup(subject2, false);
        } else {
            mode = WATCH_1;
            if (subject1 != null) cutSetup(subject1, true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        //String c = e.getKeyChar();
        int key = e.getKeyCode();

        //move camera up and down x axis
        float dx = (float) 0.1;

        switch (key) {
            case 107://K
                eye.x -= dx;
                focus.x -= dx;
                return;
            case 108://L
                eye.x += dx;
                focus.x += dx;
                return;
            case 109://m
                eye.y += dx;
                focus.y += dx;
                return;
            case 110://n
                eye.y -= dx;
                focus.y -= dx;
                return;

            case 49: //1
                setMode(WATCH_1);
                return;
            case 50: //2
                setMode(WATCH_2);
                return;
            case 51: //3
                setMode(WATCH_PLAN);
                return;
            case 52: //4
                setMode(WATCH_TILE);
                return;
            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}



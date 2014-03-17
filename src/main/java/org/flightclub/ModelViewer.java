/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.applet.Applet;
import java.awt.*;

public class ModelViewer extends Panel implements ClockObserver {
    ModelCanvas modelCanvas = null;
    Obj3dManager obj3dManager = null;
    EventManager eventManager = null;
    CameraMan cameraMan = null;
    Landscape landscape = null;    //hack
    Sky sky = null;
    ModelEnv modelEnv;
    String textMessage = null;
    Compass compass = null;
    DataSlider slider = null;
    Clock clock = null;
    boolean pendingStart = false;
    protected static final int frameRate = 25;
    //how much model time elapses during each tick, say 1/25 of a model time unit (a minute)
    protected float timePerFrame = (float) (1.0 / 25);

    ModelViewer() {
    }

    void init(ModelEnv theModelEnv) {
        modelEnv = theModelEnv;
        createClock();
        createModelCanvas();
        createObj3dManager();
        createEventManager();
        createCameraMan();
    }

    public void start() {
        if (clock != null) clock.start();
        else pendingStart = true;
    }

    public void stop() {
        if (clock != null) clock.stop();
    }

    protected void createClock() {
        clock = new Clock(1000 / frameRate);
        clock.addObserver(this);
        if (pendingStart) start();
    }

    public void tick(Clock c) {
        eventManager.tick();
        modelCanvas.tick();
        cameraMan.tick();
        modelCanvas.repaint(); //TODO
    }

    protected void createModelCanvas() {
        setLayout(new BorderLayout());
        add("Center", modelCanvas = new ModelCanvas(this));

	/*
      If running as an applet we must set the size
	  of this panel. Works fine without this call when
	  running as a stand alone app. In the later case
	  the following call makes the canvas too tall so
	  that the score etc disappeear off the bottom off
	  the screen.
	*/
        try {
            Applet a = (Applet) modelEnv;
            setSize(a.getSize().width, a.getSize().height);
        } catch (Exception e) {
        }

        layout(); //deprecated !! what here ?!
        modelCanvas.init();
    }

    protected void createObj3dManager() {
        obj3dManager = new Obj3dManager(this);
    }

    protected void createCameraMan() {
        cameraMan = new CameraMan(this);
    }

    protected void createEventManager() {
        eventManager = new EventManager();
    }

    protected void createLandscape() {
        //hack - want camera to be able to 'see' landscape
        landscape = new Landscape(this);
    }

    protected void createSky() {
        //hack - want camera to be able to 'see' landscape
        sky = new Sky(this);
    }

    int getFrameRate() {
        return frameRate;
    }
}

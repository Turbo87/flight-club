/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.event.KeyEvent;
import java.util.Vector;

public class XCGame extends ModelViewer implements EventInterface {

    Vector<Glider> gaggle;
    GliderUser gliderUser;
    JetTrail jet1;
    JetTrail jet2;
    int mode;
    boolean fastForward = true;

    static final int MODE_DEMO = 0;
    static final int MODE_USER = 1;

    @Override
    public void init(Interface a) {
        super.init(a);
        eventManager.addNotification(this);

        createSky();
        createLandscape();

        gliderUser = new GliderUser(this, new Vector3d(0, 0, 0));
        gliderUser.landed();
        cameraMan.subject1 = gliderUser;

        jet1 = new JetTrail(this, -JetTrail.TURN_RADIUS, -JetTrail.TURN_RADIUS);
        jet2 = new JetTrail(this, 0, JetTrail.TURN_RADIUS);
        jet2.makeFlyX();

        gaggle = new Vector<>();
        for (int i = 0; i < 10; i++) {
            Glider glider;
            if (i != 3 && i != 7) {
                glider = new Glider(this, new Vector3d());
            } else {
                //pink ones
                glider = new Glider(this, new Vector3d(), false, true);
            }
            gaggle.addElement(glider);
            if (i == 5) {
                cameraMan.subject2 = glider;
                glider.triggerLoading = true;
                jet1.buzzThis = glider;
                jet2.buzzThis = glider;
            }
            //glider.triggerLoading = true;//????
        }

        cameraMan.setEye(Landscape.TILE_WIDTH / 2, -Landscape.TILE_WIDTH / 4, 6);
        cameraMan.setFocus(0, 0, 0);

        launchGaggle();
        cameraMan.setMode(CameraMan.WATCH_2);
        textMessage = "Demo mode";
        mode = MODE_DEMO;
        toggleFastForward();

    }

    void launchGaggle() {
        for (int i = 0; i < gaggle.size(); i++) {
            Glider glider = gaggle.elementAt(i);
            glider.takeOff(new Vector3d(4 - i, 4 - i, (float) 1.5));
        }
    }

    void launchUser() {
        gliderUser.takeOff(new Vector3d(4 - 4 - 1, 4 - 6, (float) 1.8));
    }

    void togglePause() {
        clock.paused = !clock.paused;
    }

    void startPlay() {
        mode = MODE_USER;
        landscape.removeAll();

        gliderUser.triggerLoading = true;
        Glider glider = gaggle.elementAt(5);
        glider.triggerLoading = false;

        launchUser();
        launchGaggle();

        cameraMan.setEye(Landscape.TILE_WIDTH / 2, -Landscape.TILE_WIDTH / 4, 6);
        cameraMan.setFocus(0, 0, 0);

        cameraMan.setMode(CameraMan.WATCH_1);
        createInstruments();

        jet1.buzzThis = gliderUser;
        jet2.buzzThis = gliderUser;

        if (clock.paused) togglePause();
        if (fastForward) toggleFastForward();
    }

    void createInstruments() {
        if (compass == null) compass = new Compass(this, 25, modelCanvas.width - 30, modelCanvas.height - 15);
        if (slider == null) {
            float vmax = -2 * Glider.SINK_RATE;
            slider = new DataSlider(this, -vmax, vmax, 30, modelCanvas.width - 60, modelCanvas.height - 15);
            slider.label = "vario";
        }
    }

    @Override
    public void tick(Clock c) {
        super.tick(c);
        if (compass != null) compass.setArrow(gliderUser.v.x, gliderUser.v.y);
        //if (slider!=null) slider.setValue(gliderUser.v.z * 150);

        //convert v from dist per frame  to dist per unit time
        if (slider != null) slider.setValue(gliderUser.v.z / timePerFrame);

    }

    /**
     * how much model time passes each second of game play
     */
    void toggleFastForward() {
        fastForward = !fastForward;
        if (fastForward)
            //2.5 minutes per second
            timePerFrame = TIME_PER_FRAME_FAST;
        else
            //0.5 minutes per second
            timePerFrame = TIME_PER_FRAME_DEFAULT;
    }

    @Override
    public void keyPressed(KeyEvent e) {

        //System.out.println(key);
        int key = e.getKeyCode();
        switch (key) {
            case 112:
            case 112 - 32:
                togglePause();
                break;
            case 121://y - start play
            case 121 - 32:
                startPlay();
                break;
            case 113://q - game speed
            case 113 - 32:
                toggleFastForward();
                break;
            case 104://h - base high
            case 104 - 32:
                sky.setHigh();
                break;
            case 108://l - base low
            case 108 - 32:
                sky.setLow();
                break;
            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}

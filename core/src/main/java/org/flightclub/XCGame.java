/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Font;
import org.flightclub.compat.Graphics;

import java.awt.event.KeyEvent;
import java.util.Vector;

public class XCGame implements EventManager.Interface, Clock.Observer {

    public enum Mode {
        DEMO,
        USER,
    }

    public static final int FRAME_RATE = 25;

    public static final float TIME_PER_FRAME = (float) (1.0 / FRAME_RATE) / 2;

    protected float timePerFrame = TIME_PER_FRAME;
    public float timeMultiplier = 1.0f;

    public float time = 0.0f;
    Vector<Glider> gaggle;
    GliderUser gliderUser;
    JetTrail jet1;
    JetTrail jet2;
    Mode mode;
    boolean fastForward = true;
    Landscape landscape = null;
    Sky sky = null;
    Compass compass = null;
    DataSlider slider = null;
    CameraMan cameraMan = null;
    EventManager eventManager = new EventManager();
    String textMessage = null;
    Obj3dManager obj3dManager = new Obj3dManager();
    Interface envInterface;
    private Variometer vario;

    final Clock clock = new Clock(1000 / FRAME_RATE);

    public XCGame() {
        clock.addObserver(this);
    }

    public void init(Interface envInterface) {
        this.envInterface = envInterface;

        cameraMan = new CameraMan(this);

        eventManager.subscribe(this);

        sky = new Sky();
        landscape = new Landscape(this);

        gliderUser = new GliderUser(this, new Vector3d(0, 0, 0));
        gliderUser.landed();
        cameraMan.subject1 = gliderUser;

        vario = new Variometer(this, gliderUser);

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
        cameraMan.setMode(CameraMan.Mode.GAGGLE);
        textMessage = "Demo mode";
        mode = Mode.DEMO;
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
        time = 0;
    }

    void togglePause() {
        clock.paused = !clock.paused;
    }

    public void start() {
        clock.start();
    }

    public void stop() {
        clock.stop();
    }

    void startPlay() {
        mode = Mode.USER;
        landscape.removeAll();

        gliderUser.triggerLoading = true;
        Glider glider = gaggle.elementAt(5);
        glider.triggerLoading = false;

        launchUser();
        launchGaggle();

        cameraMan.setEye(Landscape.TILE_WIDTH / 2, -Landscape.TILE_WIDTH / 4, 6);
        cameraMan.setFocus(0, 0, 0);

        cameraMan.setMode(CameraMan.Mode.SELF);
        createInstruments();

        jet1.buzzThis = gliderUser;
        jet2.buzzThis = gliderUser;

        if (clock.paused) togglePause();
        if (fastForward) toggleFastForward();
    }

    void createInstruments() {
        if (compass == null) compass = new Compass(25, envInterface.getWidth() - 30, envInterface.getHeight() - 15);
        if (slider == null) {
            float vmax = -2 * Glider.SINK_RATE;
            slider = new DataSlider(-vmax, vmax, 30, envInterface.getWidth() - 60, envInterface.getHeight() - 15);
            slider.label = "vario";
        }
    }

    @Override
    public void tick(float delta) {
        time += delta * timeMultiplier / 2.0f;

        eventManager.processEvent();
        cameraMan.tick();

        if (compass != null) compass.setArrow(gliderUser.v.x, gliderUser.v.y);
        //if (slider!=null) slider.setValue(gliderUser.v.z * 150);

        //convert v from dist per frame  to dist per unit time
        if (slider != null) slider.setValue(2.0f * gliderUser.v.z / (delta * timeMultiplier));

        vario.tick(delta);
    }

    /**
     * how much model time passes each second of game play
     */
    void toggleFastForward() {
        fastForward = !fastForward;
        if (fastForward)
            //2.5 minutes per second
            timeMultiplier = 5.0f;
        else
            //0.5 minutes per second
            timeMultiplier = 1.0f;

        timePerFrame = TIME_PER_FRAME * timeMultiplier;
    }

    @Override
    public void keyPressed(KeyEvent e) {

        //System.out.println(key);
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_P:
                togglePause();
                break;

            case KeyEvent.VK_Y:
                startPlay();
                break;

            case KeyEvent.VK_Q:
                toggleFastForward();
                break;

            case KeyEvent.VK_H:
                sky.setHigh();
                break;

            case KeyEvent.VK_G:
                sky.setLow();
                break;

            case KeyEvent.VK_K:
                cameraMan.move(-CameraMan.CAMERA_MOVEMENT_DELTA, 0);
                return;
            case KeyEvent.VK_L:
                cameraMan.move(CameraMan.CAMERA_MOVEMENT_DELTA, 0);
                return;
            case KeyEvent.VK_M:
                cameraMan.move(0, CameraMan.CAMERA_MOVEMENT_DELTA);
                return;
            case KeyEvent.VK_N:
                cameraMan.move(0, -CameraMan.CAMERA_MOVEMENT_DELTA);
                return;

            case KeyEvent.VK_1:
                cameraMan.setMode(CameraMan.Mode.SELF);
                return;
            case KeyEvent.VK_2:
                cameraMan.setMode(CameraMan.Mode.GAGGLE);
                return;
            case KeyEvent.VK_3:
                cameraMan.setMode(CameraMan.Mode.PLAN);
                return;
            case KeyEvent.VK_4:
                cameraMan.setMode(CameraMan.Mode.TILE);
                return;

            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void draw(Graphics g, int width, int height) {
        //TODO optimize - build vector of objs in FOV, need only draw these
        cameraMan.setMatrix();

        obj3dManager.sortObjects();
        for (ObjectLayer layer : obj3dManager.layers) {
            for (Object3d object : layer) {
                object.film(cameraMan);
                object.draw(g);
            }
        }

        //Text
        if (textMessage != null) {
            Font font = new Font("SansSerif", Font.PLAIN, 10);
            g.setFont(font);
            g.setColor(Color.LIGHT_GRAY);

            String s;
            if (!clock.paused) {
                s = textMessage;
            } else {
                s = textMessage + " [ paused ]";
            }
            g.drawString(s, 15, height - 15);
        }

        if (compass != null)
            compass.draw(g);

        if (slider != null)
            slider.draw(g);
    }
}


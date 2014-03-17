/*
  XCModel.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.client;

import flightclub.framework3d.*;
import java.io.IOException;
import java.awt.event.*;
/**
   This class implements the top level manager for the flight club
   game.
*/
public class XCModel extends Model {
    XCModelViewer xcModelViewer;
    GliderManager gliderManager;
    Task task;
    Compass compass = null;
    DataSlider slider = null;

    /** An intermediate var gives us a casted reference to the camera
     * man. I'm not sure if this is good style. What if the camera man
     * object changes ? Then this link points to the wrong object !*/
    private XCCameraMan xcCameraMan; 
    int mode;

    static final int DEMO = 0;
    static final int USER = 1;

    public XCModel(XCModelViewer xcModelViewer) {
	super(xcModelViewer);
	this.xcModelViewer = xcModelViewer;
	xcCameraMan = (XCCameraMan) xcModelViewer.cameraMan;
    }

    // don't want the super classes model (a cube).
    protected void makeModel() {;}

    /**
       Loads the task. This involves downloading a file so may take a
       while over the net. Being unable to load the task is a *fatal*
       error. Well it would be, but we create a dummy task in this
       case. This means the applet does *something* when the task file
       has gone belly up !
     */
    public void loadTask(String id, int pilotType, int[] typeNums) {
	String msg;

	if (id == null || id.equals("") || id.equals("default")) {
	    msg = "Loading default task...";
	    xcModelViewer.modelCanvas.setText(msg, PROMPT_LINE);
	    System.out.println(msg);
	    task = new Task(xcModelViewer); // default task
	} else {
	    xcModelViewer.modelCanvas.setText("Loading task: " + id + "...", PROMPT_LINE);
	    try {
		task = new Task(xcModelViewer, id);
	    } catch (Exception e) {
		msg = "Error loading task: " + id + "\n" + e;
		xcModelViewer.modelCanvas.setText(msg, PROMPT_LINE);
		System.out.println(msg);
		System.exit(1); // ?
	    } 
	}

	gliderManager = new GliderManager(xcModelViewer, pilotType);
        if (!xcModelViewer.netFlag) {
	    if (typeNums != null) {
		gliderManager.createAIs(typeNums[0], typeNums[1], typeNums[2]);
	    } else {
		// defaults 
		gliderManager.createAIs(3, 3, 3);
	    }
	}
    }

    private boolean userPlay_ = false; // flag true *after* calling startPlay
    /**
       Starts game play. The first call to this fn puts game into demo
       mode. Subsequent calls (when user presses <y>) launch the user
       glider.
    */
    void startPlay() {
	xcCameraMan.gotoTaskStart();
	if (userPlay_) { // user pressed <y>
	    gliderManager.launchUser();
	    xcCameraMan.setMode(XCCameraMan.USER);
	    promptTakeOff(false);

	    if (!doneInstruments) {
		createInstruments();
		mode = USER;
	    }
	}

        if (xcModelViewer.xcNet == null) {
	    gliderManager.launchAIs();
	    task.nodeManager.loadNodes(0, xcModelViewer.clock.getTime());
	} else {
	    if (xcModelViewer.netTimeFlag) {
		task.nodeManager.loadNodes(0, xcModelViewer.clock.getTime());
	    }
	}

	if (!userPlay_) {
	    if (xcModelViewer.xcNet == null) {
		xcCameraMan.setMode(XCCameraMan.GAGGLE);
	    } else {
		// stay put ?
	    }
	    promptTakeOff(true);
	    mode = DEMO;
	}

	// check these toggles are off
	if (modelViewer.clock.paused) togglePause();
	if (modelViewer.clock.speedy) toggleFastForward();

	userPlay_ = true;
    }	

    /**
       How much model time passes each second of game play ?  Either 1
       second (normal) or 10 seconds (speedy). Speedy time is handy
       for cloud watching.
    */
    void toggleFastForward() {
	modelViewer.clock.speedy = !modelViewer.clock.speedy;
    } 

    void togglePause() {
	modelViewer.clock.paused = !modelViewer.clock.paused;
    }


    private boolean doneInstruments = false;
    /**
       Creates the compass and vario.
    */
    private void createInstruments() {
	int width = modelViewer.modelCanvas.getSize().width;
	int height = modelViewer.modelCanvas.getSize().height;
	float vmax = -2 * gliderManager.gliderUser.getSink(1);
	
	compass = new Compass(modelViewer, 25, width - 30, height- 15);
	slider = new DataSlider(modelViewer, -vmax, vmax, 30, width - 60, height - 15);
	slider.label = "vario";
	doneInstruments = true;
    }

    private float t_ = 0;
    private static final float T_INTERVAL = 0.2f;
    static final int GLIDER_LINE = 2;
    static final int SERVER_LINE = 1;
    static final int PROMPT_LINE = 0; // 0 is bottom line
    /**
       If we are in user mode then update the instruments and the status
       messages.
    */
    public void tick(float t, float dt) {
	if (t < t_ + T_INTERVAL) {
	    return;
	}

	t_ = t;

	if (mode == USER) {
	    Glider g = gliderManager.gliderUser;
	    compass.setArrow(g.v[0], g.v[1]);
	    slider.setValue(g.getSink() + g.air[2]);

	    modelViewer.modelCanvas.setText(g.getStatusMsg(), GLIDER_LINE);
	    if (g.getLanded()) {
		promptTakeOff(true);
	    }
	}

	// server status
	serverStatus();

    	// frame rate for when testing etc
	//status += " (F: " + modelViewer.clock.getFrameRate() + ")"; //tmp
    }	


    /**
       Display server info. Lumped camera status in here aswell !
    */
    void serverStatus() {
	String s;
	if (xcModelViewer.xcNet == null) {
	    s = "Offline";
	} else {
	    int n = 1 + gliderManager.numNet;
	    s = n + " pilots online";
	}
	modelViewer.modelCanvas.setText(s + ", " 
					+ xcCameraMan.getStatusMsg(), SERVER_LINE);
    }

    public void keyPressed(KeyEvent e) {
	int key = e.getKeyCode();
	System.out.println("Key: " + key);

	switch (key) {
	case 112://p - pause (offline only)
	case 112-32:
	    if (xcModelViewer.xcNet == null) {
		togglePause(); 
	    }
	    break;
	case 121://y - start play
	case 121-32:
	    if (prompting) {
		startPlay(); break;
	    }
	case 113://q - game speed (offline only)
	case 113-32:
	    if (xcModelViewer.xcNet == null) {
		toggleFastForward();
	    }
	    break;
        case 49: //1
            xcCameraMan.setMode(XCCameraMan.USER);
            return;
        case 50: //2
            xcCameraMan.setMode(XCCameraMan.GAGGLE);
            return;
        case 51: //3
            xcCameraMan.setMode(XCCameraMan.PLAN);
            return;
        case 52: //4
            xcCameraMan.setMode(XCCameraMan.NODE);
            return;
        case 53: //5
            xcCameraMan.setMode(XCCameraMan.TASK);
            return;
        case 54: //6
            xcCameraMan.setMode(XCCameraMan.PILOT);
            return;
        case 55: //7
            xcCameraMan.setMode(XCCameraMan.STAY_THERE);
            return;
        case 45: //-
            xcCameraMan.pullOut();
            return;
        case 61: //+
            xcCameraMan.pullIn();
            return;
        case 37://left arrow
	    if (prompting) {
		cycleType(-1);
	    }
	    break;
        case 39://right arrow
	    if (prompting) {
		cycleType(1);
	    }
	    break;
	default:
	}
    }

    public void keyReleased(KeyEvent e) {;}

    private int gliderType = 1; //user's glider type
    /**
       Cycles thru the glider types. 
    */
    void cycleType(int dir) {
	gliderType += dir;
	if (gliderType < 0) {
	    gliderType = GliderManager.NUM_TYPES - 1;
	}
	if (gliderType >= GliderManager.NUM_TYPES) {
	    gliderType = 0;
	}
	promptTakeOff(true); // update display
	gliderManager.createUser(gliderType); 
	xcCameraMan.setMode(XCCameraMan.USER); // look at my new glider
    }

    private boolean prompting = false;

    /**
       Prompts user to press y to take off. Set flag to false to clear
       this prompt when user has taken off.
    */
    void promptTakeOff(boolean flag) {
	String s;
	String ss = "";

	// list of glider types - highlight the current choice
	for (int i = 0; i < GliderManager.NUM_TYPES; i++) {
	    if (gliderType == i) {
		ss += " <" + GliderManager.typeNames[i] + "> ";
	    } else {
		ss += "  " + GliderManager.typeNames[i] + "  ";
	    }
	}

	if (flag) {
	    s = "Press <y> to take off. Use left/right arrow keys to choose your wing ("
		+ ss + ").";
	} else {
	    s = "";
	}
	modelViewer.modelCanvas.setText(s, PROMPT_LINE);
	prompting = flag;
    }

}


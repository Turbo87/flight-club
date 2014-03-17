/*
 * @(#)ModelViewer.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

import flightclub.startup.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.applet.*;
/**
  This is the main manager (the hub) of the framework. We have factory
  methods for creating other managers. Most (all?)  objects hold a
  reference back to ModelViewer. The ModelViewer can be displayed
  using either a frame or an applet.

  @see ModelFrame
  @see ModelApplet 
*/
public class ModelViewer extends Panel implements ClockObserver, ModelViewerThin {
    public ModelCanvas modelCanvas = null;
    public Obj3dManager obj3dManager = null;
    public EventManager eventManager = null;
    public CameraMan cameraMan = null;
    public ModelEnv modelEnv;
    public Clock clock = null;
    public Model model = null;
    protected boolean pendingStart = false;
    protected boolean debug = false; // TODO
    	
    public ModelViewer() {
	createControls();
	createModelCanvas();
    }
	
    public void init(ModelEnv modelEnv) {
	this.modelEnv = modelEnv;
	modelCanvas.init();
	createCameraMan();
	createClock();
	createObj3dManager();
	createEventManager();
	createModel();
    }

    public void start() {
	if (clock != null) {
	    clock.start();
	} else {
	    pendingStart = true;
	}
	System.out.println("Start clock!");
    }
	
    public void stop(){
	System.out.println("Model viewer stopping");
	if (clock != null) {
	    clock.stop();
	}
    }
	
    /**
       Creates the clock. Pass in the current model time; this will be
       zero unless we are connected to a game server. Or, perhaps we generate
       a random time in the former case ?
    */
    protected void createClock() {
	clock = new Clock(0);
	clock.addObserver(this);
	if (pendingStart) start();
    }
	
    /**
       This looks a bit fishy ! But trust me. Dispatching tick events
       from here to the event manager, model canvas and cameraman gives
       'bullet time' when the clock is paused.
    */
    public void tick(float t, float dt) {
	eventManager.tick();
	modelCanvas.tick();
	cameraMan.tick();
	modelCanvas.paintModel();
	modelCanvas.repaint();

	// uncomment next line to see frame rate
	//modelCanvas.setText("F: " + clock.getFrameRate());
    }

    protected void createModelCanvas() {
	setLayout(new BorderLayout());
	add("Center", modelCanvas = new ModelCanvas(this));
	validate();
    }

    protected void createObj3dManager() {
	obj3dManager = new Obj3dManager(this);
    }

    protected void createCameraMan() {
	cameraMan = new CameraMan(this);
	cameraMan.init();
    }
	
    protected void createEventManager() {
	eventManager = new EventManager();
    }

    /**
       Creates a unit cube at the origin. Override this method to
       create your own model. Obviously !
    */
    protected void createModel() {
	model = new Model(this);
    }

    public boolean getDebug() {
	return debug;
    }

    /**
       Adds some buttons to the south for zooming in and out and
       toggling the camera subject. Override this method with an empty
       stub if you do not want any controls.
    */
    protected void createControls() {
	Button b;
	Panel p = new Panel();

	b = new Button("Zoom In");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cameraMan.pullIn();
		}
	    });
	p.add(b);

	b = new Button("Zoom Out");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cameraMan.pullOut();
		}
	    });
	p.add(b);

	b = new Button("Toggle Subject");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    toggleSubject();
		}
	    });
	p.add(b);

	b = new Button("||");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    pause();
		}
	    });
	p.add(b);

	this.add(p, "South");
    }

    private int subjectIndex = -1;
    private Vector subjects = new Vector();
    /**
       A list of possible camera subjects that the user may cut
       between by pressing the 'toggle subject' button.  
    */
    private void toggleSubject() {
	if (subjects.size() == 0) return; 
	if (++subjectIndex >= subjects.size()) subjectIndex = 0;
	cameraMan.setSubject((CameraSubject) subjects.elementAt(subjectIndex), true);
    }

    /** Adds a camera subject to the list of subjects. */
    public void addSubject(CameraSubject subj) {
	subjects.addElement(subj);
    }

    /** Toggles pause */
    private void pause() {
	this.clock.paused = ! this.clock.paused;
    }

    public boolean handleEvent (KeyEvent e) {
	return eventManager.handleEvent(e);
    }
}

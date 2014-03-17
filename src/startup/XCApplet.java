/*
  @(#)XCApplet.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.startup;

import java.awt.*;
/**
   This class implements the Flight Club applet. The task id is read from
   the web page.
*/
public class XCApplet extends ModelApplet implements ModelEnv, Runnable {
    String task;
    int pilotType; // 0 - paraglider, 1 - hangglider, 2 - sailplane
    String hostPort = null; // connection to game server
    int[] typeNums = null; // numbers of AI gliders (for single player game)
    Thread thread;
    boolean finishedLoading = false;

    public XCApplet() {
	setBackground(new Color(0xFFFFFF));
    }

    private Font font = new Font("SansSerif", Font.PLAIN, 10);

    public void paint(Graphics g) {
	g.setFont(font);
	g.setColor(Color.lightGray);
	g.drawString("Flight Club is loading" + dots, 20, 20);
    }

    /**
       Starts a stub which dynamically loads the main progam. Also our splash thread.
    */
    public void start(){
	if (thread == null) {
	    thread = new Thread(this);
	    thread.start();
	}

	XCLoader loader = new XCLoader(this);
	loader.start();
    }

    private String dots = "";

    // provide user feedback while loading main classes
    public void run() {
	while (!finishedLoading) {
	    dots += ".";
	    repaint();
	    try {
		Thread.sleep(500);
	    } catch (Exception e) {;}
	}
    }

    void loadFat() {
	// dynamic loading
	ModelViewerThin x = null;
	try {
	    Class c = Class.forName("flightclub.client.XCModelViewer");
	    x = (ModelViewerThin) c.newInstance();
	} catch (Exception e) {
	    System.out.println(e);
	    System.exit(1);
	}
	super.init(x);
	x.start();
	finishedLoading = true;
    }

    public void init() {
	repaint(); //display loading message
	parseParams();
    }

    private void parseParams() {
	this.task = getParameter("task");

	try {
	    this.pilotType = parseInt(getParameter("pilot_type"));
	} catch (Exception e) {
	    pilotType = 1; //default hg
	}

	int port = -1;
	try {
	    port = parseInt(getParameter("port"));
	} catch (Exception e) {
	    port = -1; //default - no game server
	}
	
	if (port > 0) { // networked game
	    hostPort = this.getCodeBase().getHost() + ":" + port;
	    System.out.println("Game server: " + hostPort);
	} else {
	    // how many of each type of AI glider
	    int pg, hg, sp;
	    try {
		pg = parseInt(getParameter("pg"));
		hg = parseInt(getParameter("hg"));
		sp = parseInt(getParameter("sp"));
	    } catch (Exception e) {
		pg = 2;
		hg = 5;
		sp = 2;
	    }
	    typeNums = new int[] {pg, hg, sp};
	}
    }

    public String getTask() { return task; }
    public int getPilotType() { return pilotType; }
    public String getHostPort() { return hostPort; }
    public int[] getTypeNums() { return typeNums; }

    public String[][] getParameterInfo() {
	String[][] info = {
	    {"task", "string", "The id of the task to be loaded."},
	    {"pilot_type", "integer", "The pilot type (0 - paraglider, 1 - hangglider, 2 - sailplane)."},
	    {"port", "integer", "The port for connecting to the game server (set to -1 for single player mode)."},
	    {"pg", "integer", "The number of AI paragliders."},
	    {"hg", "integer", "The number of AI hang-gliders."},
	    {"sp", "integer", "The number of AI sailplanes."},
	};
	return info;
    }

    public String getAppletInfo() {
        return "Title: Flight Club \nAuthor: Dan Burton \nAn online gliding simulator.";
    }

    /**
       VM 1.1 compliant methods for parsing numbers. Methods copied
       here from Tools3d as that package is not loaded
       yet. (Hopefully)
    */
    final static float parseFloat(String s) {
	return Float.valueOf(s).floatValue();
    }

    final static int parseInt(String s) {
	return Integer.valueOf(s).intValue();
    }

}

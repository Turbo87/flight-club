/*
  XCModelViewer.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Dan Burton , Nov 2001 
*/
package flightclub.client;

import flightclub.framework3d.*;
import java.awt.*;
import java.applet.*;
import java.io.*;
/**
   This class simply overrides a few factory methods to create the
   model viewer for the xc model.
*/
public class XCModelViewer extends ModelViewer {
    XCModel xcModel;
    boolean netFlag = false; //true; // flag - net game or single player 
    boolean netTimeFlag = false; // flag set to true when we first recieve the model time from the server
    XCNet xcNet = null; // connection to server with a send method

    /** Connects to game server. */
    void connectToServer() {
	try {
	    xcNet = new XCNet(this); 
	    xcNet.start();
	} catch(IOException e){
	    System.out.println("Error connecting to game server: "+ e);
	    // degrade to single player mode
	    // todo: display server offline msg
	}
    }

    //  Type chaining - ha !
    protected void createModel() {
	model = xcModel = new XCModel(this);
    }

    protected void createCameraMan() {
	cameraMan = new XCCameraMan(this);
	cameraMan.init();
    }

    /**
       Overrides the start method to include loading the task. Also
       connect to the game server if running in networked game mode.

       Design ? create glider manager first, then xcNet then task ?
       xcNet must be after gliders.
     */
    public void start() {
	if (clock != null) {
	    setNetFlag();
	    xcModel.loadTask(this.modelEnv.getTask(), this.modelEnv.getPilotType(), this.modelEnv.getTypeNums());
	    if (netFlag) {
		connectToServer(); 
	    }

	    if (netFlag && xcNet == null) { // unable to connect !
		xcModel.gliderManager.createAIs(3, 3, 3);
	    }

	    clock.start();
	    xcModel.startPlay();
	} else {
	    pendingStart = true;
	}
    }

    /**
       Flag - connect to game server only if a host and port have been
       specified.
    */
    private void setNetFlag() {
	netFlag = (modelEnv.getHostPort() != null);
    }

    /**
       No controls (zoom button etc.) at bottom of the screen. They
       were just for use with the test harnesses.
    */
    protected void createControls() {
    }

    protected void createModelCanvas() {
	setLayout(new BorderLayout());
	add("Center", modelCanvas = new XCModelCanvas(this));
	validate();
    }

    public void stop() {
	super.stop();
	if (xcNet != null) {
	    xcNet.destroyMe(); // close socket
	}
    }
}

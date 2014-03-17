/*
  @(#)ModelApplet.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.startup;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import java.io.*;
/**
   This class implements an applet within which runs a ModelViewer. We
   use a 'thin' interface to the model viewer so the applet may load
   quickly. 
*/
public class ModelApplet extends Applet implements ModelEnv {
    ModelViewerThin modelViewerThin = null;
	
    public ModelApplet() {; }
	
    public void init(ModelViewerThin x) {
	modelViewerThin = x;
	setBackground(Color.white);
	setLayout(new BorderLayout());
	add("Center", (Panel) modelViewerThin);
	validate();
	modelViewerThin.init((ModelEnv) this);

	this.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e){
		    //modelViewer.eventManager.handleEvent(e);
		    modelViewerThin.handleEvent(e);
		}
		public void keyReleased(KeyEvent e){
		    modelViewerThin.handleEvent(e);
		}
	    });
	this.requestFocus(); 
    }
	
    public void start(){ modelViewerThin.start(); }
    public void stop(){ modelViewerThin.stop(); }
	
    public void destroy(){}
	
    public Image getImage(String s) {
	URL url = getCodeBase();
	return super.getImage(url, s);
    }
	
    public void play(String s){play(getCodeBase(), s);}

    public InputStream openFile(String name) {
	try {
	    //URL url = new URL(getCodeBase().toString() + name);
	    URL url = new URL(getDocumentBase(), name);
	    return url.openStream();
	} catch (MalformedURLException e) {
	    System.out.println("Bad file name " + name);
	} catch (IOException e) {
	    System.out.println("Error reading file " + name);
	}
	return null;
    }

    public String getTask() { return null; }
    public int getPilotType() { return 0; }
    public String getHostPort() { return null; }
    public int[] getTypeNums() { return null; }
}

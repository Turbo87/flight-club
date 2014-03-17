/*
  @(#)XCLoader.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.startup;
import java.lang.Thread;
/**
   A stub for XCModelViewer. Allows applet to provide feedback while
   classes are being loaded.
*/
public class XCLoader extends Thread {
    private XCApplet applet;

    public XCLoader(XCApplet applet) {
	this.applet = applet;
    }

    public void run() {
	applet.loadFat();
    }
}

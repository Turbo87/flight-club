/*
  @(#)XCFrame.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.startup;

import flightclub.framework3d.*;
/**
   This class implements Flight Club as an application. We read params
   from the command line...

   > XCFrame [task] [pilot_type] [host:port] [pgs hgs sps]

   All the params are optional. If none are passed in the game loads
   defaults as follows: task = default, pilot_type = 1 (hg), host:port =
   null (ie. single player mode), pgs = 2, hgs = 4, sps = 2.

   The last two params are mutually exclusive. Either we connect to a
   game server or we create a number of AI gliders.
*/
public class XCFrame extends ModelFrame {
    public XCFrame(String task, int pilotType, String hostPort, int[] typeNums){ 
	super("flightclub.client.XCModelViewer", "Flight Club", 700, 370, task, pilotType, hostPort, typeNums); 
    }

    public static void main(String s[]) { 
	String task;
	int pilotType;
	String hostPort;
	int[] typeNums = new int[3];

	if (s.length < 1) {
	    task = "default";
	} else {
	    task = s[0];
	}

	if (s.length < 2) {
	    pilotType = 1; //0 = pg, 1 = hg, 2 = sp
	} else {
	    pilotType = parseInt(s[1]);
	}

	if (s.length < 3) {
	    hostPort = null;
	    typeNums = new int[] {2, 5, 2};
	} else {
	    if (s[2].indexOf(":") > 0) {
		hostPort = s[2];
		typeNums = null;
	    } else {
		hostPort = null;
		try {
		    for (int i = 0; i < 3; i++) {
			typeNums[i] = parseInt(s[i + 2]);
		    }
		} catch (Exception e) {
		    System.out.println("Error reading AI glider numbers: " + e);
		    typeNums = new int[] {2, 5, 2};	    
		}
	    }
	}
	XCFrame x = new XCFrame(task, pilotType, hostPort, typeNums); 
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

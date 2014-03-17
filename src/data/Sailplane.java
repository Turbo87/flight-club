/*
 * @(#)Sailplane.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.data;

import flightclub.framework3d.*;
import flightclub.client.GliderType;
import java.awt.*;
import java.util.*;
import java.io.IOException;
/**
   This class outputs a text file of data for a sailplane. See
   GliderType for a definition of the file format. The file
   sailplane.txt is output to the current directory.  
*/
public class Sailplane extends GliderType {
    public Sailplane() {
	super("sailplane");
	this.obj = new Sailplane3d(null, false);
	this.polar = new float[][] {{0.8f, -0.8f/12}, {1.2f, -1.2f/8}};
	this.turnRadius = 0.4f;
    }

    public static void main (String[] args) {
	Sailplane s = new Sailplane();
	System.out.println(s.toString());
	try {
	    s.writeFile();
	    System.out.println("Wrote the data file for the sailplane.");
	} catch (IOException e) {
	    System.out.println("Error: " + e);
	}
    }
}

/**
   This class implements the *shape* of a sailplane.
*/
class Sailplane3d extends Obj3dDir {
    public Sailplane3d(ModelViewer app, boolean register) {
	super(app, 6 + Person.NUM_POLYGONS, register);
	init();
    }

    private void init() {
	/*
	  Each of the four wing panels has width one unit.  Once the
	  glider has been constructed we reduce this by a scale factor

	 plan        y
	             |
	             |
	              ------x
            
                     |
	  ----- ----- ----- -----
	 |  a  |  b  | c   | d   |
	  ----- ----- ----- -----
	             |
		   --|--
		  |-----|e          f is the rudder

	front	     z
		     |
		     |
		      ------x
          	      /\
		     /  \R
		    /    \
		   /      \    The wings bend up with a radius of curvature R (~4).
		
	      ---___  ---  ___---
		    ---|--- */


	final float scaleModel = 0.12f; 

	double R = 5.0;
	float h1 = (float) (R * (1 - Math.cos(1.0/R)));//bend of inner wing panels due to weight
	float h2 = (float) (R * (1 - Math.cos(2.0/R)));//bend of outer wing panels due to weight

	float w0 = 0.2f;//width at center
	float taper = 0.1f * w0; //tips thinner the center of wing
	float w1 = w0 - taper;//width at end of 1st panel
	float w2 = w1 - taper;//width at tips
	float at = w2 * 0.2f;//nose a bit up

	//add panels in the order c, d, b, a
	this.addPolygon2(new float[][] {{0, 0, at}, {1, 0, h1 + at}, {1, -w1, h1}, {0, -w0, 0}});
	this.addPolygon2(new float[][] {{1, 0, h1 + at}, {2, 0, h2 + at}, {2, -w2, h2}, {1, -w1, h1}});
	this.addPolygon2(new float[][] {{-1, 0, h1 + at}, {0, 0, at}, {0, -w0, 0}, {-1, -w1, h1}});
	this.addPolygon2(new float[][] {{-2, 0, h2 + at}, {-1, 0, h1 + at}, {-1, -w1, h1}, {-2, -w2, h2}});

	/** Tailplane */
	float tspan = 0.3f;
	float tw = 0.1f;
	float th = 0.3f;
	float length = 0.5f;
	float y1 = -length;
	float y2 = -(length + tw);
	float dy = 0.3f * th;

	//add panels e and f
	// moved tailplane to top of rudder
	//this.addPolygon2(new float[][] {{-tspan, y1, 0}, {tspan, y1, 0}, {tspan, y2, 0}, {-tspan, y2, 0}});
	this.addPolygon2(new float[][] {{-tspan, y1 - dy, th}, {tspan, y1 - dy, th}, {tspan, y2 - dy, th}, {-tspan, y2 - dy, th}});
	this.addPolygon2(new float[][] {{0, y1, 0}, {0, y1 - dy, th}, {0, y2 - dy, th}, {0, y2, 0}});

	// Fuselage - thanks to Tom Mayne <tom.mayne@blueyonder.co.uk>
	// this.addPolygon2(new float[][] {{0, y1, 0}, {0, 0, -0.1f}, {0, 0, 0}});

	this.scaleBy(scaleModel);
	this.translateBy(0, 0, 0.015f - (at * scaleModel));
	Person.addPilot(this, Person.SP);
    }
}


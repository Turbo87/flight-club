/*
 * @(#)Balloon.java (part of 'Flight Club')
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
   This class outputs a text file of data for a balloon. See
   GliderType for a definition of the file format. The file
   balloon.txt is output to the current directory.

   This is a hack - a balloon is not really a glidertype, but this
   sort of works - see the glidertypes test harness. 

   The polar does not apply for a balloon. We fudge it by setting a
   very low speed and zero sink. Any sink in the polar makes the
   balloon pitch. We do not want that !
*/
public class Balloon extends GliderType {

    Balloon() {
	super("balloon");
	obj = new Balloon3d(null, false);
	polar = new float[][] {{0.01f, 0}, {0.01f, 0}}; 
	turnRadius = 0.4f;
    }

    public static void main (String[] args) {
	Balloon p = new Balloon();
	System.out.println(p.toString());
	try {
	    p.writeFile();
	    System.out.println("Wrote the data file for the Balloon.");
	} catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	}
    }
}

class Balloon3d extends Obj3dDir {
    public Balloon3d(ModelViewer modelViewer, boolean register) {
	super(modelViewer, 32 + Person.NUM_POLYGONS, register);
	init();
    }

    private void init() {
        /* There are two elements within model: Balloon & pilot
	   the balloon is made using lathePolygons which takes a line of points
	   and spins them round and colours them with the colour list.*/

   	//this.lathePolygons(new float[][] {{0,0.1f,0.2f},{0, 0.5f, 0.7f},{0,0.75f,1.2f},{0,0.5f,1.7f},{0,0.00001f, 1.8f}}, new Color[] {Color.red, Color.blue}, 8);
	this.lathePolygons(new float[][] {{0,0.1f/2,0.11f},{0, 0.5f/2, 0.7f/2},{0,0.75f/2,1.2f/2},{0,0.5f/2,1.7f/2},{0,0.00001f, 1.8f/2}}, new Color[] {Color.white, Color.gray}, 8);
	// dan - make balloon only half as big, no color, PG style pilot, circular
	/*
	int N = 5;
	float R = 0.3f;
	float z0 = 0.11f + R;
	float[][] ps = new float[5][3];
	double t = 0;

	for (int i = 0; i < N; i++) {
	    ps[i][1] =  R * (float) Math.sin(t);
	    ps[i][2] =  z0 - R * (float) Math.cos(t);
	    t += Math.PI/(N - 1); 
	}

	this.lathePolygons(ps, new Color[] {Color.yellow, Color.white}, 8);
	*/
	Person.addPilot(this, Person.STANDING);
    }
}


/*
 * @(#)HangGlider.java (part of 'Flight Club')
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
   This class outputs the text file of data for a hangglider. See
   GliderType for a definition of the file format. The file
   hangglider.txt is output to the current directory. 
*/
public class HangGlider extends GliderType {
    HangGlider() { 
	super("hangglider"); 
	obj = new HangGlider3d(null, false);
	polar = new float[][] {{0.5f, - 0.5f/8}, {0.75f, -0.75f/5}};
	//polar = new float[][] {{1f, - 1f/8}, {1.5f, -1.5f/5}};
	turnRadius = 0.3f; // 3.01 was 0.3f 
    }

    public static void main (String[] args) {
	HangGlider h = new HangGlider();
	System.out.println(h.toString());
	try {
	    h.writeFile();
	    System.out.println("Wrote the data file for the hangglider.");
	} catch (IOException e) {
	    System.out.println("Error: " + e);
	}
    }
}

/**
   This class implements the *shape* of a hang glider.
*/
class HangGlider3d extends Obj3dDir {
    public HangGlider3d(ModelViewer app, boolean register) { 
	super(app, 2 + 5, register); 
	init(); 
    }

    private void init() {
	/*
	  Each of the two wing panels has width one unit.  Once the
	  glider has been constructed we reduce this by a scale factor.

	 plan        y
	             |
	             |
	              ------x
		      .
                    /   \
                  /   .   \
	        /   /   \   \    
	      / a /       \ b \ 
	     |  /           \  |
	     |/               \|
	              
		          
	*/
	
	final float scaleModel = 0.15f; // 3.01 was 0.2f

	float y = 0.2f;//chord
	float z = y * 0.3f;//nose a bit up
	float a = 0.15f;//anhedral
	float s = 0.4f;//sweep

	Color c = Obj3d.COLOR_DEFAULT;

	//add panels b then a
	/*
	this.addPolygonWithShadow(new float[][] {{0, 0, z}, {0, -y, 0}, {1, -y - s, a}, {1, - s, z + a}}, 
				  c, true);
	this.addPolygonWithShadow(new float[][] {{0, 0, z}, {-1, -s, z + a}, {-1, -y - s, a}, {0, -y, 0}}, 
				  c, true);
	*/

	this.addPolygonWithShadow(new float[][] {{0, 0, z}, {1, - s, z + a}, {1, -y - s, a}, {0, -y, 0}}, 
				  c, true);
	this.addPolygonWithShadow(new float[][] {{0, 0, z}, {0, -y, 0}, {-1, -y - s, a}, {-1, -s, z + a}}, 
				  c, true);

	this.translateBy(0, y, 0);
	this.scaleBy(scaleModel);
	this.translateBy(0, - 0.015f, 0.015f);
	Person.addPilot(this, Person.HG); // ?

	//old (no shadows)...
	//this.addPolygon2(new float[][] {{0, 0, z}, {1, - s, z + a}, {1, -y - s, a}, {0, -y, 0}});
	//this.addPolygon2(new float[][] {{0, 0, z}, {0, -y, 0}, {-1, -y - s, a}, {-1, -s, z + a}});

    }
}


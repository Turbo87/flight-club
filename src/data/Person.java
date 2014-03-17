/*
 * @(#)Person.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.data;

import flightclub.framework3d.*;
import java.awt.Color;
/**
   This class implements a person. The shape is based on a design by
   Artem Nikulchev <jaguart@paragliding.ru> for his paraglider pilot.

   A person may appear in various guises - a sailplane pilot, a hang
   glider pilot, a paraglider pilot...

   People are important. People ARE important. They will crop up again
   and again in Flight Club. The fundamental unit of distance is 0.1 -
   the height of a person ! Much as architectural drawings have little
   people in them, our Flight Club models (and unit tests) include
   people.  

   The black rectangular obilisk in Stanley Kubrick's film '2001 - a
   Space Oddessy' is *magical*. People are magical.  
*/
public class Person {
    public final static int NUM_POLYGONS = 5;
    public final static float SIZE = 0.1f;

    public final static int PG = 0;
    public final static int HG = 1;
    public final static int SP = 2;
    public final static int STANDING = 3;

    /** Adds a person to a 3d object. */
    private static void addPerson(Obj3d o, int mode) {
	/**
	   the pilot faces +y:

	   z
	   ^
	   |
	   |
	   |
	   .------->y

	   we define three points:

	   ps[0]
	   .
	   |\
	   | \
	   |  \
	   |   \
	   .----. ps[1]
	  ps[2]  

	  and use them to build a solid with 5 sides:

	   .-------.              .
	   |       |              |\
	   |       |             b| \f
	   | back  |             a|  \r
	   |       |             c|   \o
	   |       |             k|    \n
	   .-------.              |     \t
	    \bottom \             |diago-\
	     \       \            |nal sides
	      .-------.           .--------\

	*/

	float scaleModel = 0.05f;

	float[][] ps;
	float dx = 0.15f;

	//define a triangle in x = 0
	switch (mode) {
	case PG:
	    //head up
	    ps = new float[][] {{0, 0, 0.5f}, {0, 0.7f, 0}, {0, 0.2f, 0}};
	    break;
	case HG:
	    //head first
	    ps = new float[][] {{0, -1, 0.3f}, {0, 0, 0.3f}, {0, 0, 0}};
	    break;
	case SP:
	    //feet first
	    ps = new float[][] {{0, 0, 0.3f}, {0, 1.0f, 0}, {0, 0, 0}};
	    break;
	case STANDING:
	    //head up standing
	    ps = new float[][] {{0, 0, 1}, {0, 0.3f, 0}, {0, 0, 0}};
	    break;
	default:
	    return;
	}

	//use this triangle to define the two sides of our pilot
	float[][] psL = new float[3][3];
	float[][] psR = new float[3][3];

	for (int i = 0; i < 3; i++) {
	    psL[i][0] = -dx;
	    psL[i][1] = ps[i][1];
	    psL[i][2] = ps[i][2];
	}

	for (int i = 0; i < 3; i++) {
	    psR[i][0] = dx;
	    psR[i][1] = ps[i][1];
	    psR[i][2] = ps[i][2];
	}

	//scale the points
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		psL[i][j] *= scaleModel;
		psR[i][j] *= scaleModel;
	    }
	}
	
	// what color - will end up gray no doubt !
	Color color = new Color(255, 255, 0); //yellow
	//Color color = new Color(210, 150, 150); // pink
	
	//now add the five polygons...

	//front, bottom and back
	o.addPolygon(new float[][] {psR[0], psR[1], psL[1], psL[0]}, color);
	o.addPolygon(new float[][] {psL[2], psL[1], psR[1], psR[2]}, color);
	o.addPolygon(new float[][] {psR[2], psR[0], psL[0], psL[2]}, color);

	//left side and right side
	o.addPolygon(new float[][] {psL[0], psL[1], psL[2]}, color);
	o.addPolygon(new float[][] {psR[0], psR[2], psR[1]}, color);
    }

    /** Adds a pilot to the wing. */
    public static void addPilot(Obj3d o, int mode) {
	addPerson(o, mode);
    }

    /** Returns a person standing at the origin facing +y. */
    public static Obj3d createPerson(ModelViewer modelViewer) {
	Obj3d obj = new Obj3d(modelViewer, NUM_POLYGONS);
	addPerson(obj, STANDING);
	return obj;
    }

}


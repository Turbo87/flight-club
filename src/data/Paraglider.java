/*
 * @(#)Paraglider.java (part of 'Flight Club')
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
   This class outputs a text file of data for a paraglider. See
   GliderType for a definition of the file format. The file
   paraglider.txt is output to the current directory.  

   The shape of a paraglider and pilot was written by Artem
   Nikulchev <jaguart@paragliding.ru>
*/
public class Paraglider extends GliderType {

    Paraglider() { 
	super("paraglider"); 
	obj = new Paraglider3d(null, false);
	polar = new float[][] {{0.4f, - 0.4f/7}, {0.6f, -0.6f/4}};
	turnRadius = 0.2f;
    }

    public static void main (String[] args) {
	Paraglider p = new Paraglider();
	System.out.println(p.toString());
	try {
	    p.writeFile();
	    System.out.println("Wrote the data file for the paraglider.");
	} catch (IOException e) {
	    System.out.println("Error: " + e.toString());
	}
    }
}

class Paraglider3d extends Obj3dDir {
    public Paraglider3d(ModelViewer modelViewer, boolean register) {
	super(modelViewer, 6 + Person.NUM_POLYGONS, register);
	init();
    }

    private void init() {
                /* There is two elements within model: paraglider & pilot

                                      .       |       .
                        .             |       |       |          .
                  .     |             |       |       |          |       .
                     C* |      B*     |   A*  |   A   |     B    |   C   |
                  .     |             |       |       |          |       .
                        .             |       |       |          .
                                      .       |       .




                & pilot


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
                                                     bottom
                */

	float scaleModel = 0.1f;

	float Xa = 0.25f;
	float Xb = 0.8f;
	float X0 = 0;
	float X1 = 1;

	float Y0  = 0;
	float Y1  = 0.3f;
	float Yb  = 0.24f;
	float Yb_= 0.03f;
	float Yc  = 0.2f;
	float Yc_= 0.09f;

	float Z0 = 1.4f;
	float Zb = Z0 - 0.1f;
	float Zc = Z0 - 0.3f;

	float fLK = 0.1f;

	//Panels A, B and C
	this.addPolygon2(new float[][] {{X0,Y1,Z0+fLK}, {Xa,Y1,Z0+fLK}, {Xa,Y0,Z0}, {X0,Y0,Z0}});
	this.addPolygon2(new float[][] {{Xa,Y1,Z0+fLK}, {Xb,Yb,Zb+fLK}, {Xb,Yb_,Zb}, {Xa,Y0,Z0}});
	this.addPolygon2(new float[][] {{Xb,Yb,Zb+fLK}, {X1,Yc,Zc+fLK}, {X1,Yc_,Zc}, {Xb,Yb_,Zb}});

	//Panels A*, B* and C*
	this.addPolygon2(new float[][] {{-X0,Y1,Z0+fLK}, {-Xa,Y1,Z0+fLK}, {-Xa,Y0,Z0}, {-X0,Y0,Z0}});
	this.addPolygon2(new float[][] {{-Xa,Y1,Z0+fLK}, {-Xb,Yb,Zb+fLK}, {-Xb,Yb_,Zb}, {-Xa,Y0,Z0}});
	this.addPolygon2(new float[][] {{-Xb,Yb,Zb+fLK}, {-X1,Yc,Zc+fLK}, {-X1,Yc_,Zc}, {-Xb,Yb_,Zb}});
	scaleBy(scaleModel);

	Person.addPilot(this, Person.PG);
    }
}


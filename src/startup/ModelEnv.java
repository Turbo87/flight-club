/*
 * @(#)ModelEnv.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.startup;

import java.io.*;
import java.awt.*;

/**
   This interface enables a ModelViewer to be used in either an
   applet or an application (a frame).  
*/
public interface ModelEnv {
    Image getImage(String s);
    //Image createImage(int w, int h);
    void play(String s);
    //DataInputStream openFile(String s);
    InputStream openFile(String s);
    Dimension getSize();
    String getTask(); // hack - should extend interface ?
    int getPilotType(); 
    String getHostPort(); 
    int[] getTypeNums(); 
}
   

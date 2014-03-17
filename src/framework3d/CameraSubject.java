/*
 * @(#)CameraSubject.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

/**
   This interface should be implemented by any 3d object
   which may become the camera subject.
*/
public interface CameraSubject {
    public float[] getEye();
    public float[] getFocus();
}


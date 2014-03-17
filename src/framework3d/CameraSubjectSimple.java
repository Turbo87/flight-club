/*
 * @(#)CameraSubjectSimple.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

/**
  This class implements the most basic camera subject. We have two points; one defines the focus position and a second defines the eye position.

  @see CameraMan#setSubject
*/
public class CameraSubjectSimple implements CameraSubject {
    private float[] eye, focus;
    
    public CameraSubjectSimple(float[] eye,float[] focus) {
	this.eye = new float[] {eye[0], eye[1], eye[2]};
	this.focus = new float[] {focus[0], focus[1], focus[2]};
    }

    public float[] getEye() { return new float[] { eye[0], eye[1], eye[2] }; }
    public float[] getFocus() { return new float[] { focus[0], focus[1], focus[2] }; }
}

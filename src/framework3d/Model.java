/*
 * @(#)Model.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

import java.awt.event.*;
/**
   This class is defines a simple model. We have a unit cube at the
   origin. This model does not change over time and does not respond
   to keyboard events. Extend this class to build more interesting
   models.
*/
public class Model implements EventInterface, ClockObserver {
    protected ModelViewer modelViewer;

    public Model(ModelViewer modelViewer) { 
	this.modelViewer = modelViewer;
	modelViewer.eventManager.addNotification(this);
	modelViewer.clock.addObserver(this);
    }

    /** Overrride this */
    protected void makeModel() {
	Obj3d.makeCube(modelViewer);
    }

    public void tick(float t, float dt) {;}
    public void keyPressed(KeyEvent e) {;}
    public void keyReleased(KeyEvent e) {;}
}


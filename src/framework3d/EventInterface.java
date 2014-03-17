/*
  @(#)EventInterface.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.awt.*;
import java.awt.event.*;
/**
	This interface is to be implemented by objects (eg. actors)
	that respond to say a user pressing a key.
	@see EventManager 
*/
public interface EventInterface {
    public void keyPressed(KeyEvent e);
    public void keyReleased(KeyEvent e);
}

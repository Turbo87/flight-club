/*
  @(#)EventManager.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
   This class implements an event manager. Events (eg. from AWT
   components) are held in a queue. They only get dispatched when the
   Clock ticks. This will make the framework more thread safe. I hope.  

   <p>***TODO***Filter out extra keyRelease/keyPress events generated
   when holding down a key for a few seconds. This AutoKeyRepeat bug
   happens on Linux (and Windows ?).  
*/
public class EventManager {

    protected Vector objs;
    protected final static int MAX_Q = 20;
    private KeyEvent [] queue = new KeyEvent [MAX_Q];
    private int queueNum = 0;
	
    public EventManager() {
	objs = new Vector();
    }
	
    /**
     Adds an object to the list of objects to be notified when an
      event happens 
    */
    public void addNotification(Object o) {
	objs.addElement(o);
    }
	
    public void removeNotification(Object o) {
	objs.removeElement(o);
    }
	
    /**
       Adds an event to the queue for handling later
    */
    public boolean handleEvent (KeyEvent e) {
	
	if (queueNum < MAX_Q) {
	    queue[queueNum] = e;
	    queueNum ++;
	    return true;
	} else {
	    return false;
	}
    }
	
    /**
       Dispatch the event at the head of the queue.
    */
    public void tick() {
	
	KeyEvent e = popQueue();
	if (e == null) return;
		
	for (int i = 0; i < objs.size(); i++) {
	    EventInterface ei = (EventInterface) objs.elementAt(i);
	    callEventHelper(ei, e);
	}
    }
	
    private void callEventHelper(EventInterface ei, KeyEvent e) {
	switch (e.getID()) {
	case KeyEvent.KEY_RELEASED:
	    ei.keyReleased(e); break;
	case KeyEvent.KEY_PRESSED:
	    ei.keyPressed(e); break;
	default:
	}
	return;
    }
	
    private KeyEvent popQueue() {
	//return event at head of the queue or null if 
	//queue is empty
	if (queueNum ==0) return null;
		
	KeyEvent e = queue[0];

	//shuffle up one
	for (int i = 0; i < queueNum - 1; i ++) {
	    queue[i] = queue[i+1];
	}
		
	queue[queueNum - 1] = null;
	queueNum --;
	return e;
    }
}

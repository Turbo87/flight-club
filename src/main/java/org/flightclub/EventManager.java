/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * default event handler
 */
public class EventManager {
    protected final Vector<EventInterface> subscribers = new Vector<>();
    final static int MAX_Q = 20;
    Queue<KeyEvent> eventQueue = new LinkedList<KeyEvent>();

    /**
     * add an object to the list of objects to be
     * notified when an event happens
     */
    public void subscribe(EventInterface ei) {
        subscribers.add(ei);
    }

    public void unsubscribe(EventInterface ei) {
        subscribers.remove(ei);
    }

    /**
     * add event to queue
     */
    public boolean handleEvent(KeyEvent e) {
        if (eventQueue.size() < MAX_Q) {
            eventQueue.add(e);
            return true;
        } else {
            return false;
        }
    }

    /**
     * process event at head of the queue
     */
    public void tick() {
        KeyEvent e = eventQueue.poll();
        if (e == null)
            return;

        for (int i = 0; i < subscribers.size(); i++) {
            EventInterface ei = subscribers.elementAt(i);
            callEventHelper(ei, e);
        }
    }

    void callEventHelper(EventInterface ei, KeyEvent e) {
        switch (e.getID()) {
            case KeyEvent.KEY_RELEASED:
                ei.keyReleased(e);
                break;
            case KeyEvent.KEY_PRESSED:
                ei.keyPressed(e);
                break;
            default:
        }
    }
}

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

public class EventManager {

    /**
     * interface to be implemented by objects (eg actors) that respond
     *to user pressing keys
     */
    public static interface Interface {
        public void keyPressed(KeyEvent e);
        public void keyReleased(KeyEvent e);
    }

    private final static int MAX_QUEUE_LENGTH = 20;

    private final Vector<Interface> subscribers = new Vector<>();
    private final Queue<KeyEvent> events = new LinkedList<>();

    /**
     * add an object to the list of objects to be
     * notified when an event happens
     */
    public void subscribe(Interface i) {
        subscribers.add(i);
    }

    public void unsubscribe(Interface i) {
        subscribers.remove(i);
    }

    /**
     * add event to queue
     */
    public boolean addEvent(KeyEvent e) {
        if (events.size() >= MAX_QUEUE_LENGTH)
            return false;

        events.add(e);
        return true;
    }

    /**
     * process event at head of the queue
     */
    public void processEvent() {
        KeyEvent e = events.poll();
        if (e == null)
            return;

        for (Interface i : subscribers) {
            if (e.getID() == KeyEvent.KEY_RELEASED)
                i.keyReleased(e);
            else if (e.getID() == KeyEvent.KEY_PRESSED)
                i.keyPressed(e);
        }
    }
}

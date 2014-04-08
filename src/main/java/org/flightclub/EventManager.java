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

    /**
     * interface to be implemented by objects (eg actors) that respond
     *to user pressing keys
     */
    public static interface Interface {
        public void keyPressed(KeyEvent e);
        public void keyReleased(KeyEvent e);
    }

    protected final Vector<Interface> subscribers = new Vector<>();
    final static int MAX_Q = 20;
    Queue<KeyEvent> eventQueue = new LinkedList<KeyEvent>();

    /**
     * add an object to the list of objects to be
     * notified when an event happens
     */
    public void subscribe(Interface ei) {
        subscribers.add(ei);
    }

    public void unsubscribe(Interface ei) {
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
            Interface ei = subscribers.elementAt(i);
            callEventHelper(ei, e);
        }
    }

    void callEventHelper(Interface ei, KeyEvent e) {
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

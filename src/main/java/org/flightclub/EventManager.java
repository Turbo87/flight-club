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
    protected final Vector<EventInterface> objs;
    final static int MAX_Q = 20;
    Queue<KeyEvent> queue = new LinkedList<KeyEvent>();

    public EventManager() {
        objs = new Vector<>();
    }

    /**
     * add an object to the list of objects to be
     * notified when an event happens
     */
    public void addNotification(EventInterface ei) {
        objs.addElement(ei);
    }

    public void removeNotification(EventInterface ei) {
        objs.removeElement(ei);
    }

    /**
     * add event to queue
     */
    public boolean handleEvent(KeyEvent e) {
        if (queue.size() < MAX_Q) {
            queue.add(e);
            return true;
        } else {
            return false;
        }
    }

    /**
     * process event at head of the queue
     */
    public void tick() {
        KeyEvent e = queue.poll();
        if (e == null)
            return;

        for (int i = 0; i < objs.size(); i++) {
            EventInterface ei = objs.elementAt(i);
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

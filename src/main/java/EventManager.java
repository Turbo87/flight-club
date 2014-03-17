import java.awt.event.KeyEvent;
import java.util.Vector;

/*
  default event handler
  10 sep 2001
*/

class EventManager {
    protected Vector<Object> objs;
    final static int MAX_Q = 20;
    KeyEvent[] queue = new KeyEvent[MAX_Q];
    int queueNum = 0;

    public EventManager() {
        objs = new Vector<>();
    }

    /*
      add an object to the list of objects to be
      notified when an event happens
    */
    public void addNotification(Object o) {
        objs.addElement(o);
    }

    public void removeNotification(Object o) {
        objs.removeElement(o);
    }

    public boolean handleEvent(KeyEvent e) {
        //add event to queue
        if (queueNum < MAX_Q) {
            queue[queueNum] = e;
            queueNum++;
            return true;
        } else {
            return false;
        }
    }

    public void tick() {
        //process event at head of the queue
        KeyEvent e = popQueue();
        if (e == null) return;

        for (int i = 0; i < objs.size(); i++) {
            EventInterface ei = (EventInterface) objs.elementAt(i);
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

    KeyEvent popQueue() {
        //return event at head of the queue or null if
        //queue is empty
        if (queueNum == 0) return null;

        KeyEvent e = queue[0];

        //shuffle up one
        for (int i = 0; i < queueNum - 1; i++) {
            queue[i] = queue[i + 1];
        }

        queue[queueNum - 1] = null;
        queueNum--;
        return e;
    }
}

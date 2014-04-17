/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.util.Vector;

/**
 * Clock has a thread and ticks
 */
public class Clock implements Runnable {

    public interface Observer {
        public void tick(float delta);
    }

    Thread ticker = null;
    final int sleepTime;
    final Vector<Observer> observers = new Vector<>();
    public long last = 0;

    boolean paused = false;

    Clock(int t) {
        sleepTime = t;
    }

    void addObserver(Observer observer) {
        observers.addElement(observer);
    }

    void removeObserver(Observer observer) {
        observers.removeElement(observer);
    }

    public void start() {
        if (ticker == null)
            ticker = new Thread(this);
        ticker.start();
        last = System.currentTimeMillis();
    }

    public void stop() {
        if (ticker != null) ticker.stop();
        ticker = null;
    }

    @Override
    public void run() {
        while (ticker != null) {
            long now = System.currentTimeMillis();
            float delta = (now - last) / 1000.0f;
            last = now;

            for (int i = 0; i < observers.size(); i++) {
                /*
                    hack - when paused still tick the modelviewer so
					we can change our POV and unpause
				*/
                if (i == 0 || !paused) {
                    Observer c = observers.elementAt(i);
                    c.tick(delta);
                }
            }

            long timeLeft = sleepTime + now - System.currentTimeMillis();
            if (timeLeft > 0) {
                try {
                    Thread.sleep(timeLeft);
                } catch (InterruptedException e) {
                }
            }
        }
        ticker = null;
    }
}

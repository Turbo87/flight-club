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
    Thread ticker = null;
    final int sleepTime;
    final Vector<ClockObserver> observers = new Vector<>();
    public long currentTick = 0;
    public long lastTick = 0;
    public long startTick = 0;
    public long tickCount = 0;

    long totalSleep = 0;    //for throttle
    long totalSleepFrom = 0;
    boolean paused = false;

    Clock(int t) {
        sleepTime = t;
    }

    void addObserver(ClockObserver observer) {
        observers.addElement(observer);
    }

    void removeObserver(ClockObserver observer) {
        observers.removeElement(observer);
    }

    public void start() {
        if (ticker == null)
            ticker = new Thread(this);
        ticker.start();
        startTick = lastTick = currentTick = System.currentTimeMillis();
    }

    public void stop() {
        if (ticker != null) ticker.stop();
        ticker = null;
    }

    public void run() {
        while (ticker != null) {
            currentTick = System.currentTimeMillis();
            tickCount++;

            for (int i = 0; i < observers.size(); i++) {
                /*
                    hack - when paused still tick the modelviewer so
					we can change our POV and unpause
				*/
                if (i == 0 || !paused) {
                    ClockObserver c = observers.elementAt(i);
                    c.tick(this);
                }
            }
            lastTick = currentTick;

            long timeLeft = sleepTime + currentTick - System.currentTimeMillis();
            totalSleep += timeLeft;

            if (timeLeft > 0) {
                try {
                    Thread.sleep(timeLeft);
                } catch (InterruptedException e) {
                }
            }
        }
        ticker = null;
    }

    long getTickCount() {
        return tickCount;
    }

    long getSeconds() {
        return tickCount * sleepTime / 1000;
    }

    public long getAvgSleep() {
        int avg;
        if (tickCount > totalSleepFrom) {
            avg = (int) (totalSleep / (tickCount - totalSleepFrom));
            if (tickCount > totalSleepFrom + 250) {
                //reset avg every 10 seconds
                totalSleep = 0;
                totalSleepFrom = tickCount;
            }
        } else {
            avg = -1;
        }
        return avg;
    }
}

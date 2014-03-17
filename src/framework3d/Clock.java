/*
  @(#)Clock.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.util.*;
/**
   This class implements the clock that manages model time. The clock
   runs on its own thread and maintains a list of observers. Each time
   round the run loop it calls the tick method of each of its
   observers. The frame rate starts out as 25 but it may go up or down
   depending on how long it takes to execute all the observer's tick
   methods. The clock keeps track of the *model* time.  
*/
public class Clock implements Runnable {

    Thread ticker = null;
    int sleepTime;
    Vector observers = new Vector();

    private long currentTick = 0;
    long tickCount = 0;

    // for tuning the tick rate
    private long idleTime = 0;
    private long blockStart;
    private float modelTime;
    private int frameRate;
    private float modelTimePerFrame;

    public boolean paused = false;
    public boolean speedy = false; // Flag true => speed model time up by a factor of 10

    private static final int INIT_RATE = 25;
    private static final int MAX_RATE = 30; 
    private static final int BLOCK = 10; // how often do we review the frame rate ?
    private static final float MODEL_TIME_PER_SECOND = 1.0f; // units of model time per second
    private static final float MODEL_TIME_PER_TICK = MODEL_TIME_PER_SECOND/1000f;
    private static final float IDLE_PERCENT_MIN = 0.1f; // idle for at least 10% of the time - do not thrash the CPU


    /*
      Creates the clock. Pass in the current model time. This will be
      zero if you are not connected to a game server, otherwise it
      will be the current model time as defined on the server.  
    */
    public Clock(float modelTime) { 
	synchTime(modelTime);
	setFrameRate(INIT_RATE);
    }

    public void addObserver(ClockObserver observer) { observers.addElement(observer); }
    public void removeObserver(ClockObserver observer) { observers.removeElement(observer); }
	
    public void start(){
	if (ticker == null) {
	    ticker = new Thread(this);
	    ticker.setPriority(Thread.MIN_PRIORITY);
	}
	ticker.start();
	blockStart = currentTick = System.currentTimeMillis();
    }
	
    public void stop(){
	if (ticker != null) {
	    ticker = null;
	}
	ticker = null;
    }
	
    public void run(){
	float t, _t = 0, dt;

	while (ticker != null) {

	    currentTick = System.currentTimeMillis();
	    tickCount ++;
			
	    modelTime = t = getTimeNow();
	    /**
	       too jumpy
	    if (_t == 0) {
		dt = modelTimePerFrame;
	    } else {
		dt = t - _t;
	    }
	    */

	    for (int i = 0;i < observers.size();i++) {
		// when paused still tick the modelviewer so
		// we can change our POV and *un*pause !
		if (i == 0 || !paused) {
		    ClockObserver c = (ClockObserver) observers.elementAt(i);
		    c.tick(t, modelTimePerFrame);
		}
	    }
			
	    long now = System.currentTimeMillis();
	    long timeLeft = sleepTime + currentTick - now;

	    // idle for a bit
	    if (timeLeft > 0) {
		idleTime += timeLeft;
		try {Thread.sleep(timeLeft);}
		catch (InterruptedException e) {}
	    }

	    // check frame rate every so often
	    if (tickCount % BLOCK == 0) {
		reviewRate(now);
	    }
	    _t = t;
	}
	ticker = null;
    }
    
    /* Returns the current model time as defined by the run loop (discrete). */
    public final float getTime() {
	return modelTime;
    }

    /* Returns the current model time right now (continuous). */
    public final float getTimeNow() {
	return (System.currentTimeMillis() - tick_) * MODEL_TIME_PER_TICK + t_;
    }

    private long tick_;
    private float t_;
    /*
      Synchronises this client's copy of the model time with the
      master time held on the game server. 
    */
    public void synchTime(float t) {
	tick_ = System.currentTimeMillis();
	t_ = t;
    }

    private void setFrameRate(int r) {
	frameRate = r;
	sleepTime = 1000/frameRate; 
	modelTimePerFrame = MODEL_TIME_PER_SECOND/frameRate;
	if (speedy) modelTimePerFrame *= 10;
    }

    /*
      Tune the frame rate up or down depending on how long we have
      been idle over the last N ticks. 
    */
    void reviewRate(long t) {
	long elapsed = t - blockStart;
	float idlePercent = (float) idleTime/elapsed;

	if (idlePercent < IDLE_PERCENT_MIN && frameRate > 2) {
	    // working too hard so slow down
	    setFrameRate(frameRate - 2);
	} else if (frameRate < MAX_RATE) {
	    setFrameRate(frameRate + 1);
	}

	// re init vars
	idleTime = 0;
	blockStart = t;
    }

    /** Gets the current frame rate. */
    public final int getFrameRate() { return frameRate; }
}

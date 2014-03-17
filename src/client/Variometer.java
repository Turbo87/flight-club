/*
 * @(#)Variometer.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.awt.*;
/**
   This class wraps the vario beeps.
*/
public class Variometer {
    XCModelViewer xcModelViewer;
    private Glider glider;
    float[] steps; //different beeps as we go up the steps

    static final float BEEP_T = 0.2f;
    static final int NUM_BEEPS = 4; //how many different sounds
 
    public Variometer(XCModelViewer xcModelViewer, Glider glider) {
	this.glider = glider;
	this.xcModelViewer = xcModelViewer;
	this.init();
    }

    /*
      Calculates the steps at which the beep changes. We step from
      zero up to the strongest lift in the game.
    */
    private void init() {
	float liftMax = Cloud.getLift(Cloud.MAX_SIZE);
	steps = new float[NUM_BEEPS];
	for (int i = 0; i < NUM_BEEPS; i++) {
	    steps[i] = i * liftMax/NUM_BEEPS;
	}
    }

    private float t_ = 0; //time of last beep

    public void tick(float t) {
	if (t > t_ + BEEP_T){
	    t_ = t;
	    this.beep();
	}
    }

    /**
       Beeps if we are going up. Which beep depends on how strong the
       lift is.
    */
    private void beep() {
	float lift = glider.getSink() + glider.air[2];
	if (lift > 0) {
	    xcModelViewer.modelEnv.play("beep" + whichStep(lift) + ".au");
	}
    }

    private int whichStep(float lift) {
	int step = -1;
	for (int i = 0; i < NUM_BEEPS; i++) {
	    if (lift >= steps[i]) step = i;
	}
	return step;
    }
}

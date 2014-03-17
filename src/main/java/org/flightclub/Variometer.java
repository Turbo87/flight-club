/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class Variometer {
    final ModelViewer app;
    private final FlyingDot flyingDot;
    float[] steps; //different beeps as we go up the steps
    private int frame_count = 0;

    static final int FRAMES_PER_BEEP = 5;
    static final int NUM_BEEPS = 4; //how many different sounds

    public Variometer(ModelViewer app, FlyingDot flyingDot) {
        this.flyingDot = flyingDot;
        this.app = app;
        this.init();
    }

    private void init() {
    /*
	  Calculate the steps at which the beep
	  changes. The strongest lift in this game
	  is twice the glider's sink rate (under big clouds)
	*/
        float liftMax = -2 * Glider.SINK_RATE;
        steps = new float[NUM_BEEPS];
        for (int i = 0; i < NUM_BEEPS; i++) {
            steps[i] = i * liftMax / NUM_BEEPS;
        }
    }

    public void tick() {
        frame_count++;
        if (frame_count == FRAMES_PER_BEEP) {
            frame_count = 0;
            this.beep();
        }
    }

    private void beep() {
	/*
	  Beep if we are going up. Which beep depends on 
	  how strong the lift is. Note, we must convert v from 
	  dist per frame to dist per unit time.
	*/
        float lift = flyingDot.v.z / app.timePerFrame;
        if (lift > 0) {
            app.modelEnv.play("beep" + whichStep(lift) + ".au");
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

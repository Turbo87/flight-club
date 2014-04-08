/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class Variometer implements Clock.Observer {
    public static final float LIFT_MAX = -2 * Glider.SINK_RATE;

    //how many different sounds
    private static final int NUM_BEEPS = 4;
    private static final int FRAMES_PER_BEEP = 5;

    //different beeps as we go up the steps
    private static final float[] STEPS = new float[NUM_BEEPS];
    static {
        // Calculate the steps at which the beep changes. The strongest lift
        // in this game is twice the glider's sink rate (under big clouds)
        for (int i = 0; i < NUM_BEEPS; i++)
            STEPS[i] = i * LIFT_MAX / NUM_BEEPS;
    }

    final XCGame app;
    private final FlyingDot flyingDot;

    private int frame_count = 0;

    public Variometer(XCGame app, FlyingDot flyingDot) {
        this.flyingDot = flyingDot;
        this.app = app;
        app.clock.addObserver(this);
    }

    @Override
    public void tick() {
        frame_count++;
        if (frame_count == FRAMES_PER_BEEP) {
            frame_count = 0;
            this.beep();
        }
    }

    /**
     * Beep if we are going up. Which beep depends on
     * how strong the lift is. Note, we must convert v from
     * dist per frame to dist per unit time.
     */
    private void beep() {
        float lift = flyingDot.v.z / app.timePerFrame;

        String filename = filenameForLift(lift);
        if (filename != null)
            app.envInterface.play(filename);
    }

    private String filenameForLift(float lift) {
        for (int i = NUM_BEEPS - 1; i >= 0; i--)
            if (lift > STEPS[i])
                return String.format("beep%d.wav", i);

        return null;
    }
}

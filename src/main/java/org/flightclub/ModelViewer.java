/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Font;
import org.flightclub.compat.Graphics;

import java.awt.BorderLayout;
import java.awt.Panel;

public class ModelViewer extends Panel implements ClockObserver {

    protected static final int FRAME_RATE = 25;

    public static final float TIME_PER_FRAME_DEFAULT = (float) (1.0 / FRAME_RATE) / 2;
    public static final float TIME_PER_FRAME_FAST = TIME_PER_FRAME_DEFAULT * 5;

    ModelCanvas modelCanvas = null;
    Clock clock = null;
    boolean pendingStart = false;
    //how much model time elapses during each tick, say 1/25 of a model time unit (a minute)
    protected float timePerFrame = TIME_PER_FRAME_DEFAULT;

    ModelViewer() {
    }

    void init(Interface envInterface) {
        createClock();
        createModelCanvas();
    }

    public void start() {
        if (clock != null) clock.start();
        else pendingStart = true;
    }

    public void stop() {
        if (clock != null) clock.stop();
    }

    protected void createClock() {
        clock = new Clock(1000 / FRAME_RATE);
        clock.addObserver(this);
        if (pendingStart) start();
    }

    @Override
    public void tick(Clock c) {
        modelCanvas.tick();
        modelCanvas.repaint(); //TODO
    }

    protected void createModelCanvas() {
        setLayout(new BorderLayout());
        add("Center", modelCanvas = new ModelCanvas((XCGame) this));

        doLayout();
        modelCanvas.init();
    }

    int getFrameRate() {
        return FRAME_RATE;
    }
}

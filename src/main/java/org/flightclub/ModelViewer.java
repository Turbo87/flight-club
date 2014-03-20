/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.BorderLayout;
import java.awt.Panel;

public class ModelViewer extends Panel implements ClockObserver {

    protected static final int FRAME_RATE = 25;

    public static final float TIME_PER_FRAME_DEFAULT = (float) (1.0 / FRAME_RATE) / 2;
    public static final float TIME_PER_FRAME_FAST = TIME_PER_FRAME_DEFAULT * 5;

    ModelCanvas modelCanvas = null;
    final Clock clock = new Clock(1000 / FRAME_RATE);
    //how much model time elapses during each tick, say 1/25 of a model time unit (a minute)
    protected float timePerFrame = TIME_PER_FRAME_DEFAULT;

    public ModelViewer() {
        setLayout(new BorderLayout());

        clock.addObserver(this);
    }

    void init(Interface envInterface) {
        createModelCanvas();
    }

    public void start() {
        clock.start();
    }

    public void stop() {
        clock.stop();
    }

    @Override
    public void tick(Clock c) {
        modelCanvas.tick();
        modelCanvas.repaint(); //TODO
    }

    protected void createModelCanvas() {
        add("Center", modelCanvas = new ModelCanvas((XCGame) this));

        doLayout();
        modelCanvas.init();
    }
}

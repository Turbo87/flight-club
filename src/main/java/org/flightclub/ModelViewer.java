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

    ModelCanvas modelCanvas = null;
    //how much model time elapses during each tick, say 1/25 of a model time unit (a minute)

    public ModelViewer() {
        setLayout(new BorderLayout());
    }

    void init(Interface envInterface) {
        ((XCGame) this).clock.addObserver(this);
        createModelCanvas();
    }

    @Override
    public void tick(Clock c) {
        modelCanvas.tick();
        modelCanvas.repaint(); //TODO
    }

    protected void createModelCanvas() {
        modelCanvas = new ModelCanvas((XCGame) this);
        add(modelCanvas);

        doLayout();
        modelCanvas.init();
    }
}

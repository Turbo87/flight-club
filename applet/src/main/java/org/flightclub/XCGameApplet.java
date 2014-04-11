/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.applet.Applet;
import java.awt.Color;

public class XCGameApplet extends Applet {
    private final XCGame app = new XCGame();

    @Override
    public void init() {
        this.setBackground(Color.WHITE);

        ModelCanvas panel = new ModelCanvas(app);
        panel.setSize(getSize());
        add(panel);

        panel.init();
        app.init(new AppletInterface(this, panel));

        this.requestFocus();
    }

    @Override
    public void start() {
        app.start();
    }

    @Override
    public void stop() {
        app.stop();
    }
}

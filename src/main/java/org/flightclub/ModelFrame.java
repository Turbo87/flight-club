/**71611335a48a7ab37a8e29c805a310cc86437b1c
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.net.MalformedURLException;
import java.net.URL;

//the frame for stand alone play cf model applet
public class ModelFrame extends Frame implements ModelEnv {
    final ModelViewer app;

    public ModelFrame(ModelViewer theApp, String title, int w, int h) {
        super(title);
        app = theApp;
        add(app, "Center");
        setSize(w, h);
        show();
        app.init(this);
        app.start();

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                app.eventManager.handleEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                app.eventManager.handleEvent(e);
            }
        });
    }

    @Override
    public Image getImage(String s) {
        return Toolkit.getDefaultToolkit().getImage(s);
    }

    @Override
    public void play(String s) {
        //can not play sound from a frame
    }

    @Override
    public DataInputStream openFile(String name) {
        return null;
    }

    public URL getCodeBase() {
        try {
            return new URL("");
        } catch (MalformedURLException e) {
            System.out.println("Unable to get code base for model panel");
            return null;
        }
    }
}

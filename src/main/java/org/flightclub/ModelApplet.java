/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ModelApplet extends Applet implements ModelEnv {
    final ModelViewer app;

    public ModelApplet(ModelViewer theApp) {
        app = theApp;
    }

    @Override
    public void init() {
        this.setBackground(Color.white);
        add(app);
        app.init(this);

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

    @Override
    public void destroy() {}

    @Override
    public Image getImage(String s) {
        URL url = getCodeBase();
        return super.getImage(url, s);
    }

    @Override
    public void play(String s) {
        play(getCodeBase(), s);
    }

    @Override
    public DataInputStream openFile(String name) {
        try {
            URL url = new URL(getCodeBase().toString() + name);
            InputStream s = url.openStream();
            return new DataInputStream(new BufferedInputStream(s));
        } catch (MalformedURLException e) {
            System.out.println("Bad file name " + name);
        } catch (IOException e) {
            System.out.println("Error reading file " + name);
        }
        return null;
    }


    /* TODO - copied frags from Threed.java

    mdname = getParameter("model");
    try {
    scalefudge = Float.valueOf(getParameter("scale")).floatValue();
    }catch(Exception e){};
	
    public String getAppletInfo() {
    return "Title: ThreeD \nAuthor: James Gosling? \nAn applet to put a 3D model into a page.";
    }

    public String[][] getParameterInfo() {
    String[][] info = {
    {"model", "path string", "The path to the model to be displayed."},
    {"scale", "float", "The scale of the model.  Default is 1."}
    };
    return info;
    }
	
    */

}

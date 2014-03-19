package org.flightclub;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

class AppletInterface implements ModelEnv {
    private final Applet applet;

    public AppletInterface(Applet applet) {
        this.applet = applet;
    }

    @Override
    public Image getImage(String s) {
        URL url = applet.getCodeBase();
        return applet.getImage(url, s);
    }

    @Override
    public void play(String s) {
        applet.play(applet.getCodeBase(), s);
    }

    @Override
    public DataInputStream openFile(String name) {
        try {
            URL url = new URL(applet.getCodeBase().toString() + name);
            InputStream s = url.openStream();
            return new DataInputStream(new BufferedInputStream(s));
        } catch (MalformedURLException e) {
            System.out.println("Bad file name " + name);
        } catch (IOException e) {
            System.out.println("Error reading file " + name);
        }
        return null;
    }

    @Override
    public Dimension getSize() {
        return applet.getSize();
    }
}

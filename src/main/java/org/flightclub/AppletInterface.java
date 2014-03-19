package org.flightclub;

import java.applet.Applet;
import java.awt.Dimension;

class AppletInterface implements Interface {
    private final Applet applet;

    public AppletInterface(Applet applet) {
        this.applet = applet;
    }

    @Override
    public void play(String s) {
        applet.play(applet.getCodeBase(), s);
    }

    @Override
    public Dimension getSize() {
        return applet.getSize();
    }
}

package org.flightclub;

import java.applet.Applet;

class AppletInterface implements Interface {
    private final Applet applet;

    public AppletInterface(Applet applet) {
        this.applet = applet;
    }

    @Override
    public void play(String s) {
        // Deactivated for now, caused freezing
        // applet.play(applet.getCodeBase(), s);
    }
}

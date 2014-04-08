package org.flightclub;

import java.applet.Applet;

class AppletInterface implements Interface {
    private final Applet applet;
    private final ModelCanvas panel;

    public AppletInterface(Applet applet, ModelCanvas panel) {
        this.applet = applet;
        this.panel = panel;
    }

    @Override
    public int getWidth() {
        return panel.getWidth();
    }

    @Override
    public int getHeight() {
        return panel.getHeight();
    }

    @Override
    public void play(String s) {
        // Deactivated for now, caused freezing
        // applet.play(applet.getCodeBase(), s);
    }
}

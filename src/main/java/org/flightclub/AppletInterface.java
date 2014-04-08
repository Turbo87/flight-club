package org.flightclub;

import java.applet.Applet;

class AppletInterface implements Interface {
    private final Applet applet;
    private final ModelViewer panel;

    public AppletInterface(Applet applet, ModelViewer panel) {
        this.applet = applet;
        this.panel = panel;
    }

    @Override
    public int getWidth() {
        return panel.modelCanvas.width;
    }

    @Override
    public int getHeight() {
        return panel.modelCanvas.height;
    }

    @Override
    public void play(String s) {
        // Deactivated for now, caused freezing
        // applet.play(applet.getCodeBase(), s);
    }
}

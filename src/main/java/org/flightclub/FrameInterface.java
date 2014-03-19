package org.flightclub;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.DataInputStream;

class FrameInterface implements Interface {
    private final Frame frame;

    public FrameInterface(Frame frame) {
        this.frame = frame;
    }

    @Override
    public Image getImage(String s) {
        return Toolkit.getDefaultToolkit().getImage(s);
    }

    @Override
    public void play(String s) {
        // can not play sound from a frame
    }

    @Override
    public DataInputStream openFile(String s) {
        return null;
    }

    @Override
    public Dimension getSize() {
        return frame.getSize();
    }
}

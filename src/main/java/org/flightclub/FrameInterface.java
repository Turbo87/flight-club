package org.flightclub;

import java.awt.Dimension;
import java.awt.Frame;

class FrameInterface implements Interface {
    private final Frame frame;

    public FrameInterface(Frame frame) {
        this.frame = frame;
    }

    @Override
    public void play(String s) {
        // can not play sound from a frame
    }

    @Override
    public Dimension getSize() {
        return frame.getSize();
    }
}

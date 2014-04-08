package org.flightclub;

import java.awt.Frame;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

class FrameInterface implements Interface {
    private final Frame frame;
    private final ModelViewer panel;

    public FrameInterface(Frame frame, ModelViewer panel) {
        this.frame = frame;
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
        try {
            // Open an audio input stream.
            URL url = this.getClass().getClassLoader().getResource(s);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

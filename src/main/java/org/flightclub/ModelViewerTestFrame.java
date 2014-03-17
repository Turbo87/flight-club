package org.flightclub;

public class ModelViewerTestFrame extends ModelFrame {
    public ModelViewerTestFrame() {
        super(new ModelViewerTest(), "Model Viewer Test", 550, 350);
    }

    public static void main(String s[]) {
        ModelViewerTestFrame f = new ModelViewerTestFrame();
    }
}

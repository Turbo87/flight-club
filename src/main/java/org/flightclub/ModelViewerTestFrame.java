package org.flightclub;

public class ModelViewerTestFrame extends ModelFrame {
    public ModelViewerTestFrame() {
        super(new ModelViewerTest(), "Model Viewer Test", 550, 350);
    }
}

class ModelViewerTestApp {
    public static void main(String s[]) {
        ModelViewerTestFrame f = new ModelViewerTestFrame();
    }
}

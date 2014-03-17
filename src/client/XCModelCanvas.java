package flightclub.client;

import flightclub.framework3d.*;

class XCModelCanvas extends ModelCanvas {
    XCModelViewer xcModelViewer;
    public XCModelCanvas(XCModelViewer xcModelViewer) {
	super(xcModelViewer);
	this.xcModelViewer = xcModelViewer;
    }

    protected void paintModel() {
	super.paintModel();
	XCModel m = xcModelViewer.xcModel;
	if (m.mode == XCModel.USER) {
            m.compass.draw(graphicsBuffer);
	    m.slider.draw(graphicsBuffer);
        }
    }
}

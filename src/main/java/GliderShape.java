import java.awt.*;
import java.util.Vector;

class GliderShape extends Object3dWithShadow {
    Color color;
    final static float height = (float) 0.2;

    public GliderShape(ModelViewer app, boolean register) {
        /*
			default constructor - gray shape
		*/

        super(app, register);
        color = new Color(190, 190, 190); //180
        init();
    }

    public GliderShape(ModelViewer app, boolean register, Color inC) {
		/*
			pass in a color (eg for user glider)
		*/
        super(app, register);
        color = inC;
        init();
    }

    private void init() {
        Vector wire;
        float z = (float) 0.5;//1
        float y = (float) 0.7;//1

        wire = new Vector();
        wire.addElement(new Vector3d(1, 0, 0));
        wire.addElement(new Vector3d(0, 0, z));
        wire.addElement(new Vector3d(0, y, 0));
        wire.addElement(new Vector3d(1, 0, 0));
        addWire(wire, color, true);

        wire = new Vector();
        wire.addElement(new Vector3d(0, y, 0));
        wire.addElement(new Vector3d(0, 0, z));
        wire.addElement(new Vector3d(-1, 0, 0));
        wire.addElement(new Vector3d(0, y, 0));
        addWire(wire, color, true);


        wire = new Vector();
        wire.addElement(new Vector3d(0, 0, z));
        wire.addElement(new Vector3d(1, 0, 0));
        wire.addElement(new Vector3d(-1, 0, 0));
        wire.addElement(new Vector3d(0, 0, z));
        addWire(wire, color, true, true);

        wire = new Vector();
        wire.addElement(new Vector3d(1, 0, 0));
        wire.addElement(new Vector3d(0, y, 0));
        wire.addElement(new Vector3d(-1, 0, 0));
        wire.addElement(new Vector3d(1, 0, 0));
        addWireWithShadow(wire, color, true, true);

        scaleBy(height);
    }
}


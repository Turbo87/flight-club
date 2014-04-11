/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.graphics.Color;
import org.flightclub.graphics.ColorFactory;
import org.flightclub.graphics.Graphics;

import java.util.Vector;

/**
 * Three new methods added to object3d...
 *
 * 1. addWireWithShadow - the wire that casts the shadow
 * 2. updateShadow - make the shaow track the object
 * 3. drawShadow - call this when drawing the landscape segement
 * that it falls on
 *
 * 2001-10-24: change one shadow to a list of shadows (use for new glider shape)
 * 2002-02-24: offset shadow to one side, and darker
 */
public class Object3dWithShadow extends Object3d {
    static final int MAX_SHADOWS = 2;
    static final int SHADOW_COLOR = 180;
    final int[] shadowCasters = new int[MAX_SHADOWS];
    final Surface[] shadows = new Surface[MAX_SHADOWS];
    int numShadows = 0;

    Object3dWithShadow(XCGame theApp) {
        super(theApp);
        initShadow();
    }

    Object3dWithShadow(XCGame theApp, boolean register) {
        super(theApp, register);
        initShadow();
    }

    public int addWireWithShadow(Vector<Vector3d> wirePoints, Color c, boolean isSolid, boolean hasNormal) {
    /*
      do super addWire, then add a shadow using
	  the same points with z ~ 0 
	*/
        shadowCasters[numShadows] = super.addWire(wirePoints, c, isSolid, hasNormal);

        //create shadow
        Vector<Vector3d> shadowPoints = new Vector<>();

        //reverse point order so shadow faces
        //up ?? assumes shadow caster faces down always ??
        for (int i = wirePoints.size() - 1; i >= 0; i--) {
            Vector3d p = (Vector3d) wirePoints.elementAt(i);
            Vector3d q = new Vector3d(p.x, p.y, (float) -0.01); //change z so we get unique points
            shadowPoints.addElement(q);
        }

        Color color = ColorFactory.create(SHADOW_COLOR, SHADOW_COLOR, SHADOW_COLOR);
        shadows[numShadows] = new Surface(this, shadowPoints.size(), color);

        for (int i = 0; i < shadowPoints.size(); i++) {
            int pointIndex = super.addPoint(shadowPoints.elementAt(i));
            shadows[numShadows].addPoint(pointIndex);
        }

        shadows[numShadows].setNormal();
        numShadows++;
        return shadowCasters[numShadows - 1];
    }

    private void initShadow() {
        for (int i = 0; i < MAX_SHADOWS; i++) {
            shadowCasters[i] = -1;
        }
    }

    public void updateShadow() {
	/*
	  keep shadow under object.
	  the owner/creator of this object should
	  call this method each time they move the 
	  object
	*/
        for (int i = 0; i < MAX_SHADOWS; i++) {
            if (shadowCasters[i] == -1) return;
            Surface surface = (Surface) wires.elementAt(shadowCasters[i]);

            for (int j = surface.numPoints - 1; j >= 0; j--) {
                Vector3d p = points.elementAt(surface.points[j]);
                Vector3d q = points.elementAt(shadows[i].points[surface.numPoints - 1 - j]);//??
                q.set(p);
                //float doff = p.z/2;
                //q.x -= doff;
                //q.y += 0;;
                if (app.landscape != null) q.z = app.landscape.getHeight(q.x, q.y);
                else q.z = 0;
            }
        }
    }

    public void drawShadow(Graphics g) {
        for (int i = 0; i < MAX_SHADOWS; i++) {
            if (shadowCasters[i] == -1)
                return;

            if (!shadows[i].isBackFace(app.cameraMan.getEye()))
                shadows[i].draw(g);
        }
    }

    @Override
    public void draw(Graphics g) {
        // tmp - not doing z order yet
        drawShadow(g);
        super.draw(g);
    }

    public static void clone(Object3dWithShadow from, Object3dWithShadow to) {
        Object3d.clone(from, to);

        for (int i = 0; i < MAX_SHADOWS; i++) {
            if (from.shadowCasters[i] != -1) {

                PolyLine fromWire = from.wires.elementAt(from.shadowCasters[i]);
                Vector<Vector3d> toWire = new Vector<>();
                for (int j = 0; j < fromWire.points.length; j++) {
                    int k = fromWire.points[j];
                    Vector3d v = from.points.elementAt(k);
                    Vector3d q = new Vector3d(v.x, v.y, v.z);
                    toWire.addElement(q);
                }
                boolean hasNorm = (fromWire.normal != null);
                to.addWireWithShadow(toWire, fromWire.c, fromWire.isSolid, hasNorm);
            }
        }
    }
}

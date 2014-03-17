/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.util.Vector;

/**
 * manager for 3d objects
 */
public class Obj3dManager {
    /*
        13/10/2001 - add layers (cf photoshop)...
            0 - at the back
            1 - default layer (used if none specified when adding an object3d)
    */
    protected final ModelViewer app;
    final Vector<Vector<Object3d>> os;
    static final int MAX_LAYERS = 3;

    Obj3dManager(ModelViewer theApp) {
        app = theApp;
        os = new Vector<>(MAX_LAYERS);
        for (int i = 0; i < MAX_LAYERS; i++)
            os.add(new Vector<Object3d>());
    }

    public void addObj(Object3d o, int layer) {
        os.get(layer).addElement(o);
    }

    public void removeObj(Object3d o, int layer) {
        os.get(layer).removeElement(o);
    }

    public void removeAll() {
        for (Vector layer : os)
            layer.removeAllElements();
    }

    //public Object3d obj(int i){return (Object3d) os.elementAt(i);}

    //public int size(){return os.size();}

    /** sort each layer so furthest away obj is first in list */
    public void sortObjects() {
        for (int a = 0; a < MAX_LAYERS; a++) {
            if (os.get(a).size() >= 2) {
                for (int i = 0; i < os.get(a).size() - 1; i++) {
                    for (int j = i + 1; j < os.get(a).size(); j++) {
                        sortPair(a, i, j);
                    }
                }
            }
        }
    }

    public void sortPair(int layer, int i, int j) {
        Vector3d p1, p2;

        Object3d object3d1 = os.get(layer).elementAt(i);
        Object3d object3d2 = os.get(layer).elementAt(j);

        if (object3d1.points_.size() == 0 || object3d2.points_.size() == 0) {
            //System.out.println("No points ! i: " + i + ", j: " + j + ", layer: " + layer);
            //System.exit(0);
            //this happens every few minutes - WHY !?
            return;
        }

        p1 = object3d1.points_.elementAt(0);
        p2 = object3d2.points_.elementAt(0);

        if (p1.x > p2.x) {
            // first point closer than second
            os.get(layer).setElementAt(object3d2, i);
            os.get(layer).setElementAt(object3d1, j);
        }
    }
}

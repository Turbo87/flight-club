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
    final Vector<ObjectLayer> layers = new Vector<>(MAX_LAYERS);
    static final int MAX_LAYERS = 3;

    public Obj3dManager() {
        for (int i = 0; i < MAX_LAYERS; i++)
            layers.add(new ObjectLayer());
    }

    public void addObj(Object3d o, int layer) {
        layers.get(layer).add(o);
    }

    public void removeObj(Object3d o, int layer) {
        layers.get(layer).remove(o);
    }

    public void removeAll() {
        for (ObjectLayer layer : layers)
            layer.clear();
    }

    //public Object3d obj(int i){return (Object3d) os.elementAt(i);}

    //public int size(){return os.size();}

    /** sort each layer so furthest away obj is first in list */
    public void sortObjects() {
        for (ObjectLayer layer : layers)
            layer.sort();
    }
}

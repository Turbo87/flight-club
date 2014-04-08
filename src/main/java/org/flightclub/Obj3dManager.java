/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.util.Collections;
import java.util.Comparator;
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
    final Vector<Vector<Object3d>> os;
    static final int MAX_LAYERS = 3;

    public Obj3dManager() {
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
        for (Vector<Object3d> objects : os)
            Collections.sort(objects, COMPARATOR);
    }

    public static final Comparator<Object3d> COMPARATOR = new Comparator<Object3d>() {
        @Override
        public int compare(Object3d o1, Object3d o2) {
            if (o1.points_.size() == 0 || o2.points_.size() == 0)
                return 0;

            Vector3d p1 = o1.points_.get(0);
            Vector3d p2 = o2.points_.get(0);

            if (p1.x > p2.x)
                return 1;
            else if (p1.x < p2.x)
                return -1;
            else
                return 0;
        }
    };
}

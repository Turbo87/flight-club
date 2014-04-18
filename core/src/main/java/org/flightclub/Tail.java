/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;

import java.util.Vector;

/**
 * a tail of length n may be attached to a flying dot
 */
public class Tail extends Object3d {
    final int length;
    final Color color;
    private Vector3d[] tail;
    int wireEvery = 4;    //default add a wire for every 5 points

    public Tail(XCGame theApp, int length, Color color) {
        // only register top level objects with the manager
        // a tail has a parent who IS registered with the manager
        //  and is responsible for drawing and ticking its tail
        // ...er no
        super(theApp, true);
        this.length = length;
        this.color = color;
    }

    public Tail(XCGame theApp, int length, Color color, int layer) {
		/*
			as aboove but add to a specific layer
			eg zero for long jet tails, roads...
		*/
        super(theApp, true, layer);
        this.length = length;
        this.color = color;
    }

    public void init(Vector3d p) {
        tail = new Vector3d[length];

        for (int i = 0; i < length; i++) {
            tail[i] = new Vector3d(p.x, p.y - (float) i / 1000, p.z);
        }

        Vector<Vector3d> tailWire = new Vector<>();
        int j = 0;
        for (int i = wireEvery; i < length; i++) {
            if (j < 2) tailWire.addElement(tail[i]);
            j++;

            if (j == wireEvery + 1) {
                super.addWire(tailWire, color, false);
                tailWire = new Vector<>();
                tailWire.addElement(tail[i]);
                j = 1;
            }
        }
    }

    public void moveTo(Vector3d newP) {
        //newP is the current position
        for (int i = 0; i < length - 1; i++) {
            int j = length - 1 - i;
            tail[j].set(tail[j - 1]);
        }
        tail[0].set(newP);
    }

    /*
     * move entire tail to newP (e.g. after glider
     * has landed and we move it to a new position
     * to resume play
     */
    public void reset(Vector3d newP) {
        for (int i = 0; i < length - 1; i++) {
            tail[i].set(newP);
        }
    }

}
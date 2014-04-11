/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.graphics.Color;

import java.util.Vector;

/**
 * a tail of length n may be attached to a flying dot
 */
public class Tail extends Object3d {
    final int tailLength;
    final Color c;
    private Vector3d[] tail;
    int wireEvery = 4;    //default add a wire for every 5 points
    int updateEvery = 1;
    int moveCount = 0;

    public Tail(XCGame theApp, int inTailLength, Color inC) {
        // only register top level objects with the manager
        // a tail has a parent who IS registered with the manager
        //  and is responsible for drawing and ticking its tail
        // ...er no
        super(theApp, true);
        tailLength = inTailLength;
        c = inC;
    }

    public Tail(XCGame theApp, int inTailLength, Color inC, int layer) {
		/*
			as aboove but add to a specific layer
			eg zero for long jet tails, roads...
		*/
        super(theApp, true, layer);
        tailLength = inTailLength;
        c = inC;
    }

    public void init(Vector3d p) {
        tail = new Vector3d[tailLength];

        for (int i = 0; i < tailLength; i++) {
            tail[i] = new Vector3d(p.x, p.y - (float) i / 1000, p.z);
        }

        Vector<Vector3d> tailWire = new Vector<>();
        int j = 0;
        for (int i = wireEvery; i < tailLength; i++) {
            if (j < 2) tailWire.addElement(tail[i]);
            j++;

            if (j == wireEvery + 1) {
                super.addWire(tailWire, c, false);
                tailWire = new Vector<>();
                tailWire.addElement(tail[i]);
                j = 1;
            }
        }
    }

    public void moveTo(Vector3d newP) {
        moveCount++;

        if (moveCount == updateEvery) {
            moveCount = 0;
            //newP is the current position
            for (int i = 0; i < tailLength - 1; i++) {
                int j = tailLength - 1 - i;
                tail[j].set(tail[j - 1]);
            }
            tail[0].set(newP);
        }
    }

    /*
     * move entire tail to newP (e.g. after glider
     * has landed and we move it to a new position
     * to resume play
     */
    public void reset(Vector3d newP) {
        for (int i = 0; i < tailLength - 1; i++) {
            tail[i].set(newP);
        }
    }

}